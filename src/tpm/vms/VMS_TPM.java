package tpm.vms;

public class VMS_TPM {

	public static void main(String[] args) {
		if (args.length < 2 || args.length % 2 != 0) {
			System.out.println("Usage: java VMS_TPM port1 configFilePath1 [port2 configFilePath2] [...]");
			System.exit(0);
		}
		for (int i = 0; i < args.length-1; i++) {
			int port = Integer.parseInt(args[i]);
			String configPath = args[i+1];
			try {
				new Thread(new VMSServer(port, configPath)).start();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}	
		}
	}

}