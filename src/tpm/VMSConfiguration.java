package vms;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class VMSConfiguration {

	private static final String CONFIG_FILE =  "C:\\Users\\ASUS\\Desktop\\FCT\\Programacao\\Java\\workspace\\SRSC-TP2\\vmsconfig.conf";

	// key store properties
	final String keyStorePath;
	final String keyStorePassword;
	final String keyStoreType;

	// private key properties
	final String privateKeyAlias;
	final String privateKeyPassword;

	// certificate properties
	final String certificatePassword;

	// signature properties
	final String signatureAlgorithm;

	// tls properties
	final String[] tlsProtocols;
	final String[] tlsCiphersuites;
	
	final String provider;
	
	public static final VMSConfiguration config; // a singleton
	static {
		try {
			config = new VMSConfiguration();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private VMSConfiguration() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(CONFIG_FILE));

		this.keyStorePath = properties.getProperty("keyStorePath");
		this.keyStorePassword = properties.getProperty("keyStorePassword");
		this.keyStoreType = properties.getProperty("keyStoreType");
		this.privateKeyAlias = properties.getProperty("privateKeyAlias");
		this.privateKeyPassword = properties.getProperty("privateKeyPassword");
		this.certificatePassword = properties.getProperty("certificatePassword");
		this.signatureAlgorithm = properties.getProperty("signatureAlgorithm");
		this.tlsProtocols = properties.getProperty("tlsProtocols").split(",");
		this.tlsCiphersuites = properties.getProperty("tlsCiphersuites").split(",");
		this.provider = properties.getProperty("provider");
	}

}
