package tpm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class GOS_TPM {

	private static final int FILE_PERMISSIONS = 0;
	private static final int LINKS_NUMBER = 1;
	private static final int OWNER_NAME = 2;
	private static final int OWNER_GROUP = 3;
	private static final int FILE_SIZE = 4;
	private static final int TIME_LAST_MODIFICATION = 5;
	private static final int FILE_NAME = 6;
	
	
//	USER	User login name
//	PID	Process ID
//	PPID	Parent process ID
//	C	CPU utilization of process
//	STIME	Start time of process
//	TTY	Controlling workstation for the process
//	TIME	Total execution time for the process
//	CMD	Command
	
	public static void main(String[] args) throws IOException {
		// protocolo de atestação:
		// receber pedidos de atestação - correr comando ps em java e fazer hash dos processos a correr
		// assinar tudo e enviar para o cliente
	

		String lspath= "ls -lA --author /home";
		System.out.println(lspath);

		Process process = Runtime.getRuntime().exec(lspath);

		BufferedReader r = 
				new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line = null;
		while ((line = r.readLine()) != null) {
			String[] attributes = line.split("\\s+");
			for (String s : attributes) {
				System.out.print(s + " ");
			}
			System.out.println();
			System.out.println(attributes.length);
		}
	}

}
