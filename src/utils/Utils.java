package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.security.KeyStore;
import java.security.SecureRandom;

public class Utils {

	private final static char[] hexArray = "0123456789abcdef".toCharArray();

	public static String toHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] toBytes(String hex) {
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
					+ Character.digit(hex.charAt(i+1), 16));
		}
		return data;
	}

	public static int generateNounce() {
		SecureRandom random = new SecureRandom();
		int nounce = random.nextInt();
		return nounce;
	}

	public static KeyStore getOrCreateKeyStore(String path, String password) throws Exception {
		final File file = new File(path);
		final KeyStore keyStore = KeyStore.getInstance("JCEKS");
		if (file.exists()) {
			keyStore.load(new FileInputStream(path), password.toCharArray());
		}
		else {
			keyStore.load(null, null);
			keyStore.store(new FileOutputStream(path), password.toCharArray());
		}
		return keyStore;
	}
	
	public static byte[] createRandomIV(int size) {
		byte[] iv = new byte[size];
		SecureRandom r = new SecureRandom();
		r.nextBytes(iv);
		return iv;
	}

}
