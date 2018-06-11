package tpm.vms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import tpm.CommandUtils;

public class VMSService implements Runnable{

	private Socket clientSocket;

	public VMSService(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			BufferedReader r = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			
//			String lsRedisAttestation = CommandUtils.ls("-Al --author", "/home/osboxes/Desktop/SRSC/redis-4.0.9");
//			String lsRedisSrcAttestation = CommandUtils.ls("-Al --author", "/home/osboxes/Desktop/SRSC/redis-4.0.9/src"); 
//			String psRedisAttestation = CommandUtils.ps("redis-server");
//			String dockerVMSAttestation = CommandUtils.dockerps();
//			String dockerGOSAttestation = CommandUtils.dockerps();
			
			//assinar
			//enviar
			
			String m = "Welcome! Type in some words, I will reverse them.";
			w.write(m, 0, m.length());
			w.newLine();
			w.flush();
			while ((m = r.readLine()) != null) {
				if (m.equals(".")) 
					break;
				char[] a = m.toCharArray();
				int n = a.length;
				for (int i=0; i<n/2; i++) {
					char t = a[i];
					a[i] = a[n-1-i];
					a[n-i-1] = t;
				}
				w.write(a,0,n);
				w.newLine();
				w.flush();
			}
			w.close();
			r.close();
			clientSocket.close();
			
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
