package utils;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

import sun.security.x509.*;

public class GenerateKeyPair {

	public static void main(String[] args) throws Exception {
		if (args.length != 9) {
			System.out.println("Usage: GenerateKeyPair keyStorePath keyStorePassword "
					+ "keyPassword keyAlias keyAlgorithm keySize provider certificateDN certificateValidation");
		}
		
		final String keyStorePath = args[0];
		final String keyStorePassword = args[1];
		final String keyPassword = args[2];
		final String keyAlias = args[3];
		final String keyAlgorithm = args[4];
		final int keySize = Integer.parseInt(args[5]);
		final String provider = args[6];
		final String dn = args[7];
		final int validation = Integer.parseInt(args[8]);

		KeyStore keyStore = Utils.getOrCreateKeyStore(keyStorePath, keyStorePassword);

		// Criacao de par de chaves publica + privada
		KeyPairGenerator generator = KeyPairGenerator.getInstance(keyAlgorithm, provider);
		generator.initialize(keySize, new SecureRandom());
		KeyPair pair = generator.generateKeyPair();

		Certificate[] certificateChain = new Certificate[1]; 
		X509Certificate certificate = generateCertificate(dn, pair, validation);
		certificateChain[0] = certificate;

		KeyStore.PrivateKeyEntry keyStoreEntry = new KeyStore.PrivateKeyEntry(pair.getPrivate(), certificateChain);
		PasswordProtection keyPairPasswordProtection = new PasswordProtection(keyPassword.toCharArray());
		keyStore.setEntry(keyAlias, keyStoreEntry, keyPairPasswordProtection);
		keyStore.store(new FileOutputStream(keyStorePath), keyStorePassword.toCharArray());

		System.out.println("--- Private key & Certificate stored at " + keyStorePath + " ---");

		KeyStore.Entry entry = keyStore.getEntry(keyAlias, keyPairPasswordProtection);
		PrivateKey privateKeyFound = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
		X509Certificate certificateFound = (X509Certificate)((KeyStore.PrivateKeyEntry)entry).getCertificate();
		PublicKey publicKeyFound = certificateFound.getPublicKey();

		System.out.println();
		System.out.println("Alias: " + keyAlias);
		System.out.println();
		byte[] privateKeyBytes = privateKeyFound.getEncoded();
		String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);
		System.out.println("Private Key Algorithm: " + privateKeyFound.getAlgorithm());
		System.out.println("Private Key Size: " + Byte.SIZE * privateKeyBytes.length);
		System.out.println("Private Format: " + privateKeyFound.getFormat());
		System.out.println("Private Key (Base 64): " + privateKeyBase64);
		System.out.println("Private Key (Hex): " + Utils.toHex(privateKeyBytes));
		System.out.println();
		byte[] publicKeyBytes = publicKeyFound.getEncoded();
		String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
		System.out.println("Public Key Algorithm: " + publicKeyFound.getAlgorithm());
		System.out.println("Public Key Size: " + Byte.SIZE * publicKeyBytes.length);
		System.out.println("Public Format: " + publicKeyFound.getFormat());
		System.out.println("Public Key (Base 64): " + publicKeyBase64);
		System.out.println("Public Key (Hex): " + Utils.toHex(publicKeyBytes));
		System.out.println();
		System.out.println("Certificate: ");
		System.out.println(certificateFound);
		
		System.out.println("\n\nNOTA: Alternativamente utilizar o keytools...");
	}
	

	// Criar um certificado X.509 assinado pelo proprio
	private static X509Certificate generateCertificate(String dn, KeyPair pair, int days) throws Exception {
	
	  Date from = new Date();
	  Date to = new Date(from.getTime() + days * 86400000l);
	  
	  X509CertInfo info = new X509CertInfo();
	  info.set(X509CertInfo.VALIDITY, new CertificateValidity(from, to));
	  info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
	  info.set(X509CertInfo.SUBJECT, new X500Name(dn));
	  info.set(X509CertInfo.ISSUER + "." + CertificateSubjectName.DN_NAME, new X500Name(dn));
	  info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
	  info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
	  String signAlgorithm = AlgorithmId.getDefaultSigAlgForKey(pair.getPrivate());
	  AlgorithmId algo = AlgorithmId.get(signAlgorithm);
	  info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
	  info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
	 
	  X509CertImpl cert = new X509CertImpl(info);
	  cert.sign(pair.getPrivate(), signAlgorithm);

	  return cert;
	}   
	
	
}
