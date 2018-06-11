package tpm.vms;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class VMS_TPM_Server implements Runnable {
	
	private static final String keyStoreName = "/home/osboxes/Desktop/SRSC/JedisBenchmark/src/cliente/serverKeyStore.jceks";
	private static final String keyStoreType = "JCEKS";
	private static final char[] keyStorePassword = "serverkeystorepassword".toCharArray();
	private static final char[] certificatePassword = "certificatepassword".toCharArray();
//	private static final char[] keyStorePassword = "hjhjhjhj".toCharArray();
//	private static final char[] certificatePassword = "hjhjhjhj".toCharArray();

	private static final int NUM_THREADS = 10;
	
	private final int serverPort;
	private SSLServerSocket sslServerSocket;
	private ExecutorService threadPool;
	
	public VMS_TPM_Server(int port) {
		this.serverPort = port;
		this.sslServerSocket = null;
		this.threadPool = Executors.newFixedThreadPool(NUM_THREADS);
	}

	@Override
	public void run() {
		openSSLServerSocket();
		for (;;) {
			SSLSocket clientSocket = null;
			try {
				clientSocket = (SSLSocket) sslServerSocket.accept();
			} catch (IOException e) {
				throw new RuntimeException("Error accepting client connection", e);
			}
			threadPool.execute(new VMSService(clientSocket));
		}
	} 
	
	private void openSSLServerSocket() {
		try {
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(new FileInputStream(keyStoreName), keyStorePassword);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, certificatePassword);
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			sslServerSocket = (SSLServerSocket) ssf.createServerSocket(serverPort);
			
			String[] confprotocols = { "TLSv1.2" }; //TODO config file
			String[] confciphersuites = { "TLS_RSA_WITH_AES_256_CBC_SHA256" }; //TODO config file
			
			sslServerSocket.setEnabledProtocols(confprotocols);
			sslServerSocket.setEnabledCipherSuites(confciphersuites);	
		} catch (Exception e) {
			throw new RuntimeException("Cannot open server socket", e);
		}
	}
}
