package tpm.vms;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class VMSConfiguration {

	// key store properties
	final String keyStoreFile;
	final String keyStorePassword;
	final String keyStoreType;

	// private key properties
	final String keyPairAlias;
	final String keyPairPassword;

	// signature properties
	final String signatureAlgorithm;
	final String signatureAlgorithmProvider;

	// tls properties
	final String[] tlsProtocols;
	final String[] tlsCiphersuites;
	
	// diffie hellman properties
	final String dhProvider;
	
	// message digest properties
	final String messageDigestAlgorithm;
	final String messageDigestAlgorithmProvider;
	
	// attestation properties
	final String redisPath;
	final String redisSrcPath;
	
	
	VMSConfiguration(String configPath) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(configPath));

		this.keyStoreFile = properties.getProperty("keyStoreFile");
		this.keyStorePassword = properties.getProperty("keyStorePassword");
		this.keyStoreType = properties.getProperty("keyStoreType");
		this.keyPairAlias = properties.getProperty("keyPairAlias");
		this.keyPairPassword = properties.getProperty("keyPairPassword");
		this.signatureAlgorithm = properties.getProperty("signatureAlgorithm");
		this.signatureAlgorithmProvider = properties.getProperty("signatureAlgorithmProvider");
		this.tlsProtocols = properties.getProperty("tlsProtocols").split(",");
		this.tlsCiphersuites = properties.getProperty("tlsCiphersuites").split(",");
		this.dhProvider = properties.getProperty("dhProvider");
		this.messageDigestAlgorithm = properties.getProperty("messageDigestAlgorithm");
		this.messageDigestAlgorithmProvider = properties.getProperty("messageDigestAlgorithmProvider");
		this.redisPath = properties.getProperty("redisPath");
		this.redisSrcPath = properties.getProperty("redisSrcPath");
	}

}

