package cliente;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

	private static final String CONFIG_FILE = System.getProperty("user.dir") + "/src/cliente/config.conf";
	
	public static String getConfig(String key) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(CONFIG_FILE));
		return properties.getProperty(key);
	}
	
}
