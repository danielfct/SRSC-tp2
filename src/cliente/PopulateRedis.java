package cliente;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import redis.clients.jedis.Jedis;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PopulateRedis {

	public static void main(String[] args) throws Exception {

		final String keyStorePath = Configuration.getConfig("keyStorePath");
		final String keyStorePassword = Configuration.getConfig("keyStorePassword");
		final String keyStoreType = Configuration.getConfig("keyStoreType");
		
		final String secretKeyAlias = Configuration.getConfig("secretKeyAlias");
		final String secretKeyPassword = Configuration.getConfig("secretKeyPassword");
		
		final String privateKeyAlias = Configuration.getConfig("privateKeyAlias");
		final String privateKeyPassword = Configuration.getConfig("privateKeyPassword");
		
		final String macKeyAlias = Configuration.getConfig("macKeyAlias");
		final String macKeyPassword = Configuration.getConfig("macKeyPassword");
		final String macAlgorithm = Configuration.getConfig("macKeyAlgorithm");
	
		final String signatureAlgorithm = Configuration.getConfig("signatureAlgorithm");
		
		final String ciphersuite = Configuration.getConfig("ciphersuite");
		final String provider = Configuration.getConfig("provider");
		
		Map<String, String> lookupTable = new HashMap<String, String>();
		
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
		
		// retrieve secret key from keystore
		PasswordProtection keyPasswordProtection = new PasswordProtection(secretKeyPassword.toCharArray());
		KeyStore.Entry entry = keyStore.getEntry(secretKeyAlias, keyPasswordProtection);
		SecretKey secretKey = ((KeyStore.SecretKeyEntry)entry).getSecretKey();
		
		// retrieve private key from keystore
		PrivateKey privateKey = (PrivateKey)keyStore.getKey(privateKeyAlias, privateKeyPassword.toCharArray());
		
		// retrieve mac key from keystore
		Key macKey = keyStore.getKey(macKeyAlias, macKeyPassword.toCharArray());
		
		// init encryption
		Cipher cipher = Cipher.getInstance(ciphersuite, provider);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey); // TODO IV?
		
		// init signature
		Signature signature = Signature.getInstance(signatureAlgorithm, provider);
		signature.initSign(privateKey);
		
		// init mac
		Mac mac = Mac.getInstance(macAlgorithm, provider);
		mac.init(macKey);
		
		// init jedis
		Jedis jedis = new Jedis("localhost", 6379); // TODO mudar host, eventualmente tls
        jedis.connect();
        jedis.flushAll();
	
        // { E || S || M }
        // E = { citizen_id || date || amount || client_id || iban }KS
        // S = SIGN(E)KP
        // M = MAC(S)KM
        for (int i = 0; i < RedisData.DATA_LENGTH; i++) {
        	byte[] citizenId = ByteBuffer.allocate(Integer.BYTES).putInt(RedisData.citizenCardId[i]).array();
        	byte[] date = RedisData.date[i].getBytes(StandardCharsets.UTF_8);
        	byte[] amount = ByteBuffer.allocate(Integer.BYTES).putInt(RedisData.amount[i]).array();
        	byte[] clientId = ByteBuffer.allocate(Integer.BYTES).putInt(RedisData.clientId[i]).array();
        	byte[] iban = RedisData.iban[i].getBytes(StandardCharsets.UTF_8);
           	
        	byte[] data = ByteBuffer
           		.allocate(citizenId.length + date.length + amount.length + clientId.length + iban.length)
           		.put(citizenId).put(date).put(amount).put(clientId).put(iban)
           		.array();
        	// encrypt
        	byte[] cipherData = cipher.doFinal(data);
        	// sign
        	signature.update(cipherData);
        	byte[] signedData = signature.sign();
        	// mac
        	byte[] macData = mac.doFinal(signedData);
        
        	byte[] finalData = new byte[cipherData.length + signedData.length + macData.length];
        	System.arraycopy(cipherData, 0, finalData, 0, cipherData.length);
        	System.arraycopy(signedData, 0, finalData, cipherData.length, signedData.length);
        	System.arraycopy(macData, 0, finalData, signedData.length, macData.length);

        	// put new entry
        	MessageDigest md = MessageDigest.getInstance("SHA512");
        	String hexHashCitizenId = Utils.toHex(md.digest(citizenId));
        	String hexHashDate = Utils.toHex(md.digest(date));
        	String hexHashAmount = Utils.toHex(md.digest(amount));
        	String hexHashClientId = Utils.toHex(md.digest(clientId));
        	String hexHashIBAN = Utils.toHex(md.digest(iban));
        	String hexKey = String.format("%s:%s:%s:%s:%s", hexHashCitizenId, hexHashDate, hexHashAmount, hexHashClientId, hexHashIBAN);
        	String hexData = Utils.toHex(finalData);
//        	System.out.println("Setting entry #" + (i+1));
        	jedis.set(hexKey, hexData);
        	lookupTable.put(hexKey, hexData);
        	
//        	// get operation by any value
//        	String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(204098170).array()));
//        	List<String> result = lookup(lookupTable, 0);
//        	result.forEach(System.out::println);

//        	byte[] decrypt = new byte[cipherData.length];
//        	String redisDataHex = jedis.get(hexKey);
//        	byte[] redisData = Utils.toBytes(redisDataHex);
//        	System.arraycopy(redisData, 0, decrypt, 0, decrypt.length);
//        	
//        	cipher.init(Cipher.DECRYPT_MODE, secretKey);
//        	ByteBuffer plainData = ByteBuffer.wrap(cipher.doFinal(decrypt));
//        	int decipherCitizenId = plainData.getInt();
//        	byte[] decipherDate = new byte[date.length];
//        	plainData.get(decipherDate);
//        	int decipherAmount = plainData.getInt();
//        	int decipherClientId = plainData.getInt();
//        	byte[] decipherIBAN = new byte[iban.length];
//        	plainData.get(decipherIBAN);
//        	System.out.println(decipherCitizenId);
//        	System.out.println(new String(decipherDate));
//        	System.out.println(decipherAmount);
//        	System.out.println(decipherClientId);
//        	System.out.println(new String(decipherIBAN));
//        	System.out.println();
        }
        
        jedis.flushAll();
        
        jedis.close();
	}
	
	private static List<String> lookup(Map<String, String> lookupTable, String key, int column) {
		return lookupTable.entrySet()
                .stream()
                .filter(map -> map.getKey().split(":")[column].equals(key))
                .map(map -> map.getValue())
                .collect(Collectors.toList());
	}
	
	
	
}
