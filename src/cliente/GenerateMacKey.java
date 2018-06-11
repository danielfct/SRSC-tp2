package cliente;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class GenerateMacKey {
	
	public static void main(String[] args) throws Exception {

		final String keyStorePath = Configuration.getConfig("keyStorePath");
		final String keyStorePassword = Configuration.getConfig("keyStorePassword");
		final String keyPassword = Configuration.getConfig("macKeyPassword");
		final String keyAlias = Configuration.getConfig("macKeyAlias");
		final String keyAlgorithm = Configuration.getConfig("macKeyAlgorithm");
		final int keySize = Integer.parseInt(Configuration.getConfig("macKeySize"));

		KeyStore keyStore = Utils.getOrCreateKeyStore(keyStorePath, keyStorePassword);
		
		KeyGenerator kg = KeyGenerator.getInstance(keyAlgorithm);
		kg.init(keySize);
		SecretKey secretKey = kg.generateKey();
		
		KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(secretKey);
		PasswordProtection keyPasswordProtection = new PasswordProtection(keyPassword.toCharArray());
		keyStore.setEntry(keyAlias, keyStoreEntry, keyPasswordProtection);
		keyStore.store(new FileOutputStream(keyStorePath), keyStorePassword.toCharArray());
		
		System.out.println("--- Mac key stored at " + keyStorePath + " ---");
		
		KeyStore.Entry entry = keyStore.getEntry(keyAlias, keyPasswordProtection);
		SecretKey secretKeyFound = ((KeyStore.SecretKeyEntry)entry).getSecretKey();
		byte[] secretKeyBytes = secretKeyFound.getEncoded();
		String secretKeyBase64 = Base64.getEncoder().encodeToString(secretKeyBytes);
		System.out.println("Alias: " + keyAlias);
		System.out.println("Mac Key Algorithm: " + secretKeyFound.getAlgorithm());
		System.out.println("Mac Key Size: " + Byte.SIZE * secretKeyBytes.length);
		System.out.println("Mac Key Format: " + secretKeyFound.getFormat());
		System.out.println("Mac Key (Base 64): " + secretKeyBase64);
		System.out.println("Mac Key (Hex): " + Utils.toHex(secretKeyBytes));
	}
}
