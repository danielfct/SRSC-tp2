package client;

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
import java.util.concurrent.Callable;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import client.exceptions.BadReplyCodeException;
import client.exceptions.UnexceptedServerNounceException;
import client.exceptions.UnverifiedServerSignatureException;
import utils.Utils;

public class GOSAttestation implements Callable<String> {

	private static final byte ATTESTATION_REQ_CODE = 0x00;
	private static final byte ATTESTATION_REPLY_CODE = 0x01;
	private static final byte IV_SIZE = 16;

	private ClientConfiguration config;

	GOSAttestation(ClientConfiguration config) {
		this.config = config;
	}

	@Override
	public String call() throws Exception {
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

		SSLSocket c = (SSLSocket) socketFactory.createSocket(config.gosHost, config.gosPort);
		c.startHandshake();
		DataOutputStream out = new DataOutputStream(c.getOutputStream());
		DataInputStream in = new DataInputStream(c.getInputStream());

		byte[] iv = Utils.createRandomIV(IV_SIZE);

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", config.dhProvider);
		keyGen.initialize(config.dhKeysize);
		KeyPair keyPair = keyGen.generateKeyPair();
		byte[] publicKeyEncoded = keyPair.getPublic().getEncoded();

		int nounce = new SecureRandom().nextInt();
		out.writeByte(ATTESTATION_REQ_CODE);
		out.writeShort(publicKeyEncoded.length);
		out.write(publicKeyEncoded);
		out.writeUTF(config.dhCiphersuite);
		out.write(iv);
		out.writeInt(nounce);
		out.flush();

		KeyAgreement keyAgreement = KeyAgreement.getInstance("DH", config.dhProvider);
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
			throw new BadReplyCodeException();
		}
		if (serverNounce != nounce + 1) {
			throw new UnexceptedServerNounceException();
		}

		// Verificar a assinatura utilizando o certificado do servidor
		FileInputStream fin = new FileInputStream(config.gosCertificate);
		CertificateFactory f = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) f.generateCertificate(fin);
		PublicKey publicKey = cert.getPublicKey();
		String signatureAlgorithm = cert.getSigAlgName();
		Signature signature = Signature.getInstance(signatureAlgorithm, config.signatureAlgorithmProvider);
		signature.initVerify(publicKey);
		byte[] dataToSign = ByteBuffer.allocate(serverPublic.length + Integer.BYTES)
				.put(serverPublic).putInt(serverNounce)
				.array();
		signature.update(dataToSign);
		if (!signature.verify(serverSignature)) {
			throw new UnverifiedServerSignatureException();
		}

		KeyFactory keyFactory = KeyFactory.getInstance("DH", config.dhProvider);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublic);
		PublicKey serverKey = keyFactory.generatePublic(x509KeySpec);
		keyAgreement.doPhase(serverKey, true);

		// generate the key
		MessageDigest hash = MessageDigest.getInstance(config.messageDigestAlgorithm, config.messageDigestAlgorithmProvider);
		byte[] sharedSecret = hash.digest(keyAgreement.generateSecret());

		SecretKeySpec secretKey = new SecretKeySpec(sharedSecret, 0, 16, "AES");
		Cipher cipher = Cipher.getInstance(config.dhCiphersuite, config.dhProvider);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		byte[] attestation = cipher.doFinal(attestationStatus);

		out.close();
		in.close();
		c.close();

		return new String("gos=" + Utils.toHex(attestation));
	}

}
