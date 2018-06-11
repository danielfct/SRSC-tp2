package vms;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSClient {

	private static final byte ATTESTATION_REQ_CODE = 0x00;
	private static final byte ATTESTATION_REPLY_CODE = 0x01;
	private static final byte IV_SIZE = 16;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java -Djavax.net.ssl.trustStore=clienttruststore TLSClient host port");
		}
		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			SSLSocket c = (SSLSocket) socketFactory.createSocket(host, port);
			c.startHandshake();
			DataOutputStream out = new DataOutputStream(c.getOutputStream());
			DataInputStream in = new DataInputStream(c.getInputStream());

//			O cliente envia no pedido (protegido na conexão TLS):
//				> ATTESTATION REQUEST CODE || KEYSIZE || PUB- DH (1024 bits) || CIPHERSUITE || IV || Secure RANDOM NONCE
//			ATTESTATION REQUEST CODE: é um mero código de operação (ex: 0x00)
//			KEYSIZE: o tamanho do número público Diffie-Hellman do cliente
//			PUB-DH: é um número público Diffie-Hellman gerado pelo cliente para o pedido de atestação
//			CIPHERSUITE: o algoritmo, modo e padding a utilizar na cifra da atestação (e.g. AES/CBC/PKCS7Padding)
//			IV:	vetor de inicialização com tamanho 16 bytes, utilizado para iniciar a cifra da atestação (deve ser único para cada pedido)
//			NONCE: número aleatório gerado pelo cliente para o pedido de atestação


			int keySize = 1024;// TODO config
			String ciphersuite = "AES/CBC/PKCS7Padding"; //TODO config
			byte[] iv = createRandomIV();

			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC"); // TODO config
			keyGen.initialize(keySize); //TODO random secure e enviar para o servidor
			KeyPair keyPair = keyGen.generateKeyPair();
			byte[] publicKeyEncoded = keyPair.getPublic().getEncoded();

			int nounce = new SecureRandom().nextInt();
			out.writeByte(ATTESTATION_REQ_CODE);
			out.writeShort(publicKeyEncoded.length);
			out.write(publicKeyEncoded);
			out.writeUTF(ciphersuite);
			out.write(iv);
			out.writeInt(nounce);
			out.flush();

			//			> ATTESTATION RESPONSE CODE || ATTESTATION SIGNATURE || ATTESTATION STATUS
			//			ATTESTATION RESPONDE: é um mero código de operação, ex: 0x01
			//			ATTESTATION SIGNATURE: é uma assinatura digital cobrindo:
			//				- Um número público Diffie-Hellman gerado pelo módulo em causa para a resposta
			//				- A resposta o NONCE do cliente (exemplo, NONCE+1)
			//			ATTESTATION STATUS: é uma lista com um conjunto (lista) de provas de síntese correspondentes ao
			//			estado auditado. A lista é enviado cifrada com AES, usando como chave K a chave derivada do acordo
			//			Diffie Hellman resultante da troca de números públicos DH.
			
			KeyAgreement keyAgreement = KeyAgreement.getInstance("DH", "BC");// TODO config
			keyAgreement.init(keyPair.getPrivate());

			byte attestationReplyCode = in.readByte();
			int serverPublicLength = in.readShort();
			byte[] serverPublic = new byte[serverPublicLength];
			in.read(serverPublic);
			int serverNounce = in.readInt();
			int signatureLength = in.readShort();
			byte[] serverSignature = new byte[signatureLength];
			in.read(serverSignature);
			int attestationLength = in.readShort();
			byte[] attestationStatus = new byte[attestationLength];
			in.read(attestationStatus);

			if (attestationReplyCode != ATTESTATION_REPLY_CODE) {
				//TODO throw new exception
			}
			if (serverNounce != nounce+1) {
				//TODO throw new exception
			}
			
			// Verificar a assinatura utilizando o certificado do servidor
			FileInputStream fin = new FileInputStream("C:\\Users\\ASUS\\Desktop\\FCT\\Programacao\\Java\\workspace\\SRSC-TP2\\vms.cer"); //TODO config
			CertificateFactory f = CertificateFactory.getInstance("X.509"); 
			X509Certificate cert = (X509Certificate)f.generateCertificate(fin);
			PublicKey publicKey = cert.getPublicKey();
			String signatureAlgorithm = cert.getSigAlgName();
			Signature signature = Signature.getInstance(signatureAlgorithm, "BC"); //TODO config
			signature.initVerify(publicKey);
			byte[] dataToSign = ByteBuffer
					.allocate(Short.BYTES + serverPublic.length + Integer.BYTES)
					.putShort((short)serverPublic.length)
					.put(serverPublic)
					.putInt(nounce+1)
					.array();
			signature.update(dataToSign);
			if (!signature.verify(serverSignature)) {

				//TODO throw new exception
			}
			
			KeyFactory keyFactory = KeyFactory.getInstance("DH", "BC"); //TODO config
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublic);
			PublicKey serverKey = keyFactory.generatePublic(x509KeySpec);
			keyAgreement.doPhase(serverKey, true);

			// generate the key
			MessageDigest hash = MessageDigest.getInstance("SHA1", "BC");
			byte[] sharedSecret = hash.digest(keyAgreement.generateSecret());

			SecretKeySpec secretKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
			Cipher cipher = Cipher.getInstance(ciphersuite, "BC"); //TODO config
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			byte[] recovered = cipher.doFinal(attestationStatus);

			System.out.println(new String(recovered));

			out.close();
			in.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();//TODO
			System.err.println(e.toString());
		}
	}

	private static byte[] createRandomIV() { //TODO utils
		byte[] iv = new byte[IV_SIZE];
		SecureRandom r = new SecureRandom();
		r.nextBytes(iv);
		return iv;
	}

}
