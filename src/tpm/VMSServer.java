package vms;


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

import static vms.VMSConfiguration.config;

public class VMSServer implements Runnable {
	
	private static final int NUM_THREADS = 10;
	
	private final int serverPort;
	private SSLServerSocket sslServerSocket;
	private ExecutorService threadPool;
	
	public VMSServer(int port) {
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
			this.threadPool.execute(new VMSService(clientSocket));
		}
		
	} 
	
	private void openSSLServerSocket() {
		try {
			KeyStore keyStore = KeyStore.getInstance(config.keyStoreType);
			keyStore.load(new FileInputStream(config.keyStorePath), config.keyStorePassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, config.certificatePassword.toCharArray());
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			sslServerSocket = (SSLServerSocket) ssf.createServerSocket(serverPort);
			sslServerSocket.setEnabledProtocols(config.tlsProtocols);
			sslServerSocket.setEnabledCipherSuites(config.tlsCiphersuites);
		} catch (Exception e) {
			throw new RuntimeException("Cannot open socket", e);
		}
	}
}