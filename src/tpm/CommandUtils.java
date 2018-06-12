package tpm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandUtils {

	public static String ls() throws IOException {
		return ls("");
	}

	public static String ls(String options) throws IOException {
		return ls(options, "");
	}

	public static String ls(String options, String fileName) throws IOException {
		String result = "";
		String command = "ls " + options + " " + fileName;
		Process process = Runtime.getRuntime().exec(command);
		BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = r.readLine()) != null) {
			if (line.contains("src") || 
					line.contains("utils") ||
					line.contains("dump.rdb")) // ficheiros que o redis modifica automaticamente
				continue;
			result += line;
		}
		return result;
	}
	
	public static String ps(String processName) throws IOException {
		String result = "";
		// regex para excluir o processo grep do resultado
		String processNameRegex = String.format("\"[%c]%s\"", processName.charAt(0), processName.substring(1));
		// array cmd necessario para utilizar pipe (e redirects)
		String[] cmd = { "/bin/sh", "-c", "ps -e -o user,command | grep " + processNameRegex };
		Process process = Runtime.getRuntime().exec(cmd);
		BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = r.readLine()) != null) {
			result += line;
		}
		return result;
	}

	public static String dockerps() {
		return "";
	}


}
