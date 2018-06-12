package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientConfiguration {

	// key store properties
	final String keyStoreFile;
	final String keyStorePassword;
	final String keyStoreType;
	
	// secret key properties
	final String secretKeyAlias;
	final String secretKeyPassword;
	final String ciphersuite;
	final String ciphersuiteProvider;
	
	// pair key properties
	final String keyPairAlias;
	final String keyPairPassword;
	
	// mac key properties
	final String macKeyAlias;
	final String macKeyPassword;
	final String macAlgorithm;
	final String macAlgorithmProvider;

	// signature properties
	final String signatureAlgorithm;
	final String signatureAlgorithmProvider;
	
	// tpm services properties
	final String vmsHost;
	final int vmsPort;
	final String vmsCertificate;
	final String vmsEvidence;
	
	final String gosHost;
	final int gosPort;
	final String gosCertificate;
	final String gosEvidence;
	
	// diffie hellman properties
	final String dhCiphersuite;
	final String dhProvider;
	final int dhKeysize;
	
	// message digest properties
	final String messageDigestAlgorithm;
	final String messageDigestAlgorithmProvider;
	
	// redis properties
	final String redisHost;
	final int redisPort;
	
	ClientConfiguration(String configPath) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(configPath));
 
		this.keyStoreFile = properties.getProperty("keyStoreFile");
		this.keyStorePassword = properties.getProperty("keyStorePassword");
		this.keyStoreType = properties.getProperty("keyStoreType");
		this.secretKeyAlias = properties.getProperty("secretKeyAlias");
		this.secretKeyPassword = properties.getProperty("secretKeyPassword");
		this.ciphersuite = properties.getProperty("ciphersuite");
		this.ciphersuiteProvider = properties.getProperty("ciphersuiteProvider");
		this.keyPairAlias = properties.getProperty("keyPairAlias");
		this.keyPairPassword = properties.getProperty("keyPairPassword");
		this.macKeyAlias = properties.getProperty("macKeyAlias");
		this.macKeyPassword = properties.getProperty("macKeyPassword");
		this.macAlgorithm = properties.getProperty("macKeyAlgorithm");
		this.macAlgorithmProvider = properties.getProperty("macAlgorithmProvider");
		this.signatureAlgorithm = properties.getProperty("signatureAlgorithm");
		this.signatureAlgorithmProvider = properties.getProperty("signatureAlgorithmProvider");
		this.vmsHost = properties.getProperty("vmsHost");
		this.vmsPort = Integer.valueOf(properties.getProperty("vmsPort"));
		this.vmsCertificate = properties.getProperty("vmsCertificate");
		this.vmsEvidence = properties.getProperty("vmsEvidence");
		this.gosHost = properties.getProperty("gosHost");
		this.gosPort = Integer.valueOf(properties.getProperty("gosPort"));
		this.gosCertificate = properties.getProperty("gosCertificate");
		this.gosEvidence = properties.getProperty("gosEvidence");
		this.dhCiphersuite = properties.getProperty("dhCiphersuite");
		this.dhProvider = properties.getProperty("dhProvider");
		this.dhKeysize = Integer.valueOf(properties.getProperty("dhKeysize"));
		this.messageDigestAlgorithm = properties.getProperty("messageDigestAlgorithm");
		this.messageDigestAlgorithmProvider = properties.getProperty("messageDigestAlgorithmProvider");
		this.redisHost = properties.getProperty("redisHost");
		this.redisPort = Integer.valueOf(properties.getProperty("redisPort"));
	}
	
}
