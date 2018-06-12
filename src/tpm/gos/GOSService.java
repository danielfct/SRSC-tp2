package tpm.gos;

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
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import tpm.CommandUtils;
import tpm.gos.exceptions.BadRequestCodeException;
import tpm.gos.exceptions.DataReplyingException;

class GOSService implements Runnable {

	private static final byte ATTESTATION_REQ_CODE = 0x00;
	private static final byte ATTESTATION_REPLY_CODE = 0x01;
	private static final byte IV_SIZE = 16;

	private GOSConfiguration config;
	private Socket clientSocket;
	private ConcurrentHashMap.KeySetView<Integer, Boolean> nounces;

	public GOSService(GOSConfiguration config, Socket clientSocket, ConcurrentHashMap.KeySetView<Integer, Boolean> nounces) {
		this.config = config;
		this.clientSocket = clientSocket;
		this.nounces = nounces;
	}

	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());

			// obter pedido
			byte attestationRequestCode = in.readByte();
			if (attestationRequestCode != ATTESTATION_REQ_CODE) {
				throw new BadRequestCodeException();
			}
			short publicLength = in.readShort();
			byte[] clientPublicKey = new byte[publicLength];
			in.read(clientPublicKey);
			String ciphersuite = in.readUTF();
			byte[] iv = new byte[IV_SIZE];
			in.read(iv);
			int nounce = in.readInt();

			// Avoid data replying
			if (nounces.contains(nounce)) {
				throw new DataReplyingException();
			}
			nounces.add(nounce);

			// obter o numero publico enviado pelo cliente
			KeyFactory keyFactory = KeyFactory.getInstance("DH", config.dhProvider);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKey);
			PublicKey clientKey = keyFactory.generatePublic(x509KeySpec);
			DHParameterSpec clientKeyDHParams = ((DHPublicKey)clientKey).getParams();

			// gerar o nosso proprio numero publico e numero privado
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", config.dhProvider);
			keyGen.initialize(clientKeyDHParams);
			KeyPair keyPair = keyGen.generateKeyPair();

			KeyAgreement keyAgreement = KeyAgreement.getInstance("DH", config.dhProvider);
			keyAgreement.init(keyPair.getPrivate());

			byte[] publicEncoded = keyPair.getPublic().getEncoded();

			// assinar a atestacao
			KeyStore keyStore = KeyStore.getInstance(config.keyStoreType);
			keyStore.load(new FileInputStream(config.keyStoreFile), config.keyStorePassword.toCharArray());
			PrivateKey privateKey = (PrivateKey)keyStore.getKey(config.keyPairAlias, config.keyPairPassword.toCharArray());
			Signature signature = Signature.getInstance(config.signatureAlgorithm, config.signatureAlgorithmProvider);
			signature.initSign(privateKey);
			byte[] dataToSign = ByteBuffer.allocate(publicEncoded.length + Integer.BYTES)
					.put(publicEncoded).putInt(nounce+1)
					.array();
			signature.update(dataToSign);
			byte[] attestationSignature = signature.sign();

			// obter a atestacao
			// nota: mais processos a correr nativamente pod
			String psRedisAttestation = CommandUtils.ps("redis-server");
			String psNautilusAttestation = CommandUtils.ps("nautilus");
			String psGSDKeyboard = CommandUtils.ps("gsd-keyboard");
			
			System.out.println(psRedisAttestation);
			System.out.println(psNautilusAttestation);
			System.out.println(psGSDKeyboard);
			System.out.println(psRedisAttestation+psNautilusAttestation+psGSDKeyboard);
			
			// cifrar a atestacao
			MessageDigest hash = MessageDigest.getInstance(config.messageDigestAlgorithm, config.messageDigestAlgorithmProvider);
			keyAgreement.doPhase(clientKey, true);
			byte[] sharedSecret = hash.digest(keyAgreement.generateSecret());
			SecretKeySpec secretKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
			Cipher cipher = Cipher.getInstance(ciphersuite, config.dhProvider);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
			byte[] status = hash.digest((psRedisAttestation + psNautilusAttestation + psGSDKeyboard)
					.getBytes(StandardCharsets.UTF_8));
			byte[] attestationStatus = cipher.doFinal(status);

			// enviar a resposta ao cliente
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
			System.err.println(e.toString());
		}
	}

}
