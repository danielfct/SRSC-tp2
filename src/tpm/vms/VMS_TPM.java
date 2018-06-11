package tpm.vms;

public class VMS_TPM {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java VMS_TPM port1 [port2] [...]");
			System.exit(0);
		}
		for (String port : args) {
			int p = Integer.parseInt(port);
			new Thread(new VMS_TPM_Server(p)).start();
		}
	}

}