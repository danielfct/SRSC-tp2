package vms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import datastructures.LimitedSizeQueue;
import vms.exceptions.BadRequestCodeException;
import vms.exceptions.DataReplyingException;

import static vms.VMSConfiguration.config;

public class VMSService implements Runnable {

	private static final byte ATTESTATION_REQ_CODE = 0x00;
	private static final byte ATTESTATION_REPLY_CODE = 0x01;
	private static final byte IV_SIZE = 16;

	private Socket clientSocket;
	private List<Integer> nounces;

	public VMSService(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.nounces = new LimitedSizeQueue<>(100);
	}

	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());

//			O cliente envia no pedido (protegido na conexão TLS):
//				> ATTESTATION REQUEST CODE || KEYSIZE || PUB- DH (1024 bits) || CIPHERSUITE || IV || Secure RANDOM NONCE
//			ATTESTATION REQUEST CODE: é um mero código de operação (ex: 0x00)
//			KEYSIZE: o tamanho do número público Diffie-Hellman do cliente
//			PUB-DH: é um número público Diffie-Hellman gerado pelo cliente para o pedido de atestação
//			CIPHERSUITE: o algoritmo, modo e padding a utilizar na cifra da atestação (e.g. AES/CBC/PKCS7Padding)
//			IV:	vetor de inicialização com tamanho 16 bytes, utilizado para iniciar a cifra da atestação (deve ser único para cada pedido)
//			NONCE: número aleatório gerado pelo cliente para o pedido de atestação

			byte attestationRequestCode = in.readByte();
			if (attestationRequestCode != ATTESTATION_REQ_CODE) {
				throw new BadRequestCodeException();
			}
			short keyLength = in.readShort();
			byte[] clientPublicKey = new byte[keyLength];
			in.read(clientPublicKey);
			String ciphersuite = in.readUTF();
			byte[] iv = new byte[IV_SIZE];
			in.read(iv);
			int nounce = in.readInt();
			if (nounces.contains(nounce)) {
				throw new DataReplyingException();
			}
			nounces.add(nounce);

			KeyFactory keyFactory = KeyFactory.getInstance("DH", config.provider);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKey);
			PublicKey clientKey = keyFactory.generatePublic(x509KeySpec);
			DHParameterSpec clientKeyDHParams = ((DHPublicKey)clientKey).getParams();

			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", config.provider);
			keyGen.initialize(clientKeyDHParams);
			KeyPair keyPair = keyGen.generateKeyPair();

			KeyAgreement keyAgreement = KeyAgreement.getInstance("DH", config.provider);
			keyAgreement.init(keyPair.getPrivate());

			byte[] publicEncoded = keyPair.getPublic().getEncoded();
			
			
//			> ATTESTATION RESPONSE CODE || ATTESTATION SIGNATURE || ATTESTATION STATUS
//			ATTESTATION RESPONDE: é um mero código de operação, ex: 0x01
//			ATTESTATION SIGNATURE: é uma assinatura digital cobrindo:
//				- Um número público Diffie-Hellman gerado pelo módulo em causa para a resposta
//				- A resposta o NONCE do cliente (exemplo, NONCE+1)
//			ATTESTATION STATUS: é uma lista com um conjunto (lista) de provas de síntese correspondentes ao
//			estado auditado. A lista é enviado cifrada com AES, usando como chave K a chave derivada do acordo
//			Diffie Hellman resultante da troca de números públicos DH.
			
			// attestation signature
			KeyStore keyStore = KeyStore.getInstance(config.keyStoreType);
			keyStore.load(new FileInputStream(config.keyStorePath), config.keyStorePassword.toCharArray());
			PrivateKey privateKey = (PrivateKey)keyStore.getKey(config.privateKeyAlias, config.privateKeyPassword.toCharArray());
			Signature signature = Signature.getInstance(config.signatureAlgorithm, config.provider);
			signature.initSign(privateKey);
			byte[] dataToSign = ByteBuffer
					.allocate(Short.BYTES + publicEncoded.length + Integer.BYTES)
					.putShort((short)publicEncoded.length)
					.put(publicEncoded)
					.putInt(nounce+1)
					.array();
			signature.update(dataToSign);
			byte[] attestationSignature = signature.sign();
			
			// attestation status
			byte[] status = "ATESTATION_HERE".getBytes(StandardCharsets.UTF_8); //TODO
			
			// cipher attestation status
			MessageDigest hash = MessageDigest.getInstance("SHA1", config.provider);
			keyAgreement.doPhase(clientKey, true);
			byte[] sharedSecret = hash.digest(keyAgreement.generateSecret());
			SecretKeySpec secretKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
			Cipher cipher = Cipher.getInstance(ciphersuite, config.provider);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
			byte[] attestationStatus = cipher.doFinal(status);
			
			
			out.writeByte(ATTESTATION_REPLY_CODE);
			out.writeShort(publicEncoded.length);
			out.write(publicEncoded);
			out.writeInt(nounce+1);
			out.writeShort(attestationSignature.length);
			out.write(attestationSignature);
			out.writeShort(attestationStatus.length);
			out.write(attestationStatus);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();//TODO
			System.err.println(e.toString());
		}
	}

}
