package client;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import client.exceptions.AttestationException;
import redis.clients.jedis.Jedis;
import utils.Utils;

public class RedisClient {

	private final ClientConfiguration config;
	
	private final ExecutorService pool;
	private final List<Callable<String>> tpmServices;
	
	private final Map<String, String> lookupTable;
	
	private final Jedis jedis;
	
	private final Cipher cipher;
	private final SecretKey secretKey;
	private final Signature signature;
	private final Mac mac;
	
	RedisClient(ClientConfiguration config) throws Exception {
		//this.jedis = new Jedis("rediss://"+config.redisHost+":"+config.redisPort);
		this.jedis = new Jedis(config.redisHost, config.redisPort);
		this.config = config;
		
		this.pool = Executors.newFixedThreadPool(2);
		this.tpmServices = new ArrayList<>(2);
		this.tpmServices.add(new VMSAttestation(config));
		this.tpmServices.add(new GOSAttestation(config));
		
		this.lookupTable = new HashMap<String, String>();

		// get keystore instance
		KeyStore keyStore = KeyStore.getInstance(config.keyStoreType);
		keyStore.load(new FileInputStream(config.keyStoreFile), config.keyStorePassword.toCharArray());
		
		// retrieve secret key from keystore
		PasswordProtection keyPasswordProtection = new PasswordProtection(config.secretKeyPassword.toCharArray());
		KeyStore.Entry entry = keyStore.getEntry(config.secretKeyAlias, keyPasswordProtection);
		this.secretKey = ((KeyStore.SecretKeyEntry)entry).getSecretKey();
		this.cipher = Cipher.getInstance(config.ciphersuite, config.ciphersuiteProvider);
		
		// retrieve private key from keystore
		PrivateKey privateKey = (PrivateKey)keyStore.getKey(config.keyPairAlias, config.keyPairPassword.toCharArray());
		this.signature = Signature.getInstance(config.signatureAlgorithm, config.signatureAlgorithmProvider);
		this.signature.initSign(privateKey);
		
		// retrieve mac key from keystore
		Key macKey = keyStore.getKey(config.macKeyAlias, config.macKeyPassword.toCharArray());
		this.mac = Mac.getInstance(config.macAlgorithm, config.macAlgorithmProvider);
		this.mac.init(macKey);
	}

	public void doAttestation() throws Exception {
		List<Future<String>> attestations = pool.invokeAll(tpmServices, 10, TimeUnit.SECONDS);
		
		for (Future<String> results : attestations) {
			String[] result = results.get().split("=");
			String tpm = result[0];
			String attestation = result[1];
			
			String evidence = tpm.equalsIgnoreCase("vms") ? config.vmsEvidence : config.gosEvidence;
			
			// tirar o comentario para obter a atestacao inicial
//			System.out.println(tpm + " evidence: " + evidence);
//			System.out.println(tpm + " attestation: " + attestation);
			
			if (!Objects.equals(evidence, attestation)) {
				throw new AttestationException(tpm + " evidence does not match attestation");
			}
		}
	}
	
	public void doSet(int citizenId, String date, int amount, int clientId, String iban) throws Exception {
		
		byte[] citizenIdBytes = ByteBuffer.allocate(Integer.BYTES).putInt(citizenId).array();
    	byte[] dateBytes = date.getBytes(StandardCharsets.UTF_8);
    	byte[] amountBytes = ByteBuffer.allocate(Integer.BYTES).putInt(amount).array();
    	byte[] clientIdBytes = ByteBuffer.allocate(Integer.BYTES).putInt(clientId).array();
    	byte[] ibanBytes = iban.getBytes(StandardCharsets.UTF_8);
       	
    	byte[] data = ByteBuffer.allocate(citizenIdBytes.length + Integer.BYTES + dateBytes.length + 
       				amountBytes.length + clientIdBytes.length + Integer.BYTES + ibanBytes.length)
       		.put(citizenIdBytes)
       		.putInt(dateBytes.length).put(dateBytes)
       		.put(amountBytes)
       		.put(clientIdBytes)
       		.putInt(ibanBytes.length).put(ibanBytes)
       		.array();
    	
    	// encrypt
    	cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    	byte[] cipherData = cipher.doFinal(data);
    	// sign
    	signature.update(cipherData);
    	byte[] signatureData = signature.sign();
    	// mac
    	byte[] macData = mac.doFinal(signatureData);
  
    	// put new entry
    	MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm, config.messageDigestAlgorithmProvider);
    	String hexHashCitizenId = Utils.toHex(md.digest(citizenIdBytes));
    	String hexHashDate = Utils.toHex(md.digest(dateBytes));
    	String hexHashAmount = Utils.toHex(md.digest(amountBytes));
    	String hexHashClientId = Utils.toHex(md.digest(clientIdBytes));
    	String hexHashIban = Utils.toHex(md.digest(ibanBytes));
    	String hexKey = String.format("%s:%s:%s:%s:%s", hexHashCitizenId, hexHashDate, hexHashAmount, hexHashClientId, hexHashIban);
    	
    	String hexData = Utils.toHex(cipherData);
    	String hexSignature = Utils.toHex(signatureData);
    	String hexMac = Utils.toHex(macData);
    	
    	String hexValue = String.format("%s:%s:%s", hexData, hexSignature, hexMac);
    	
//    	System.out.println("Setting entry #" + (i+1));
    	
    	jedis.set(hexKey, hexValue);
    	lookupTable.put(hexKey, hexValue);
	}
	
	public List<String> doGetByCitizenId(int citizenId) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(citizenId).array()));
    	String[] redisKeys = lookup(key, 0);
    	List<String> redisValues = new ArrayList<>(redisKeys.length);
    		
    	for (String k : redisKeys) {
    		String v = doGet(k);
    		redisValues.add(v);
    	}
    	
    	return redisValues;	
	}
	
	public List<String> doGetByDate(String date) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(date.getBytes(StandardCharsets.UTF_8)));
    	String[] redisKeys = lookup(key, 1);
    	List<String> redisValues = new ArrayList<>(redisKeys.length);
    	

    	for (String k : redisKeys) {
    		String v = doGet(k);
    		redisValues.add(v);
    	}
    	
    	return redisValues;	
	}
	
	public List<String> doGetByAmount(int amount) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(amount).array()));
    	String[] redisKeys = lookup(key, 2);
    	List<String> redisValues = new ArrayList<>(redisKeys.length);
    		
    	for (String k : redisKeys) {
    		String v = doGet(k);
    		redisValues.add(v);
    	}
    	
    	return redisValues;	
	}
	
	public List<String> doGetByClientId(int clientId) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(clientId).array()));
    	String[] redisKeys = lookup(key, 3);
    	List<String> redisValues = new ArrayList<>(redisKeys.length);
    		
    	for (String k : redisKeys) {
    		String v = doGet(k);
    		redisValues.add(v);
    	}
    
    	return redisValues;
	}
	
	public List<String> doGetByIban(String iban) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(iban.getBytes(StandardCharsets.UTF_8)));
    	String[] redisKeys = lookup(key, 4);
    	List<String> redisValues = new ArrayList<>(redisKeys.length);
    	
    	for (String k : redisKeys) {
    		String v = doGet(k);
    		redisValues.add(v);
    	}
    	
    	return redisValues;	
	}
	
	public String doGet(String key) throws Exception {

		String data = jedis.get(key);
		if (data == null)
			return null;
		
		String[] redisData = data.split(":");
		String values = redisData[0];
		String signature = redisData[1];
		String mac = redisData[2];
		
    	byte[] cipherValues = Utils.toBytes(values);
    	
    	cipher.init(Cipher.DECRYPT_MODE, secretKey);
    	ByteBuffer plainData = ByteBuffer.wrap(cipher.doFinal(cipherValues));
    	int citizenId = plainData.getInt();
    	byte[] date = new byte[plainData.getInt()];
    	plainData.get(date);
    	int amount = plainData.getInt();
    	int clientId = plainData.getInt();
    	byte[] iban = new byte[plainData.getInt()];
    	plainData.get(iban);
    	
    	return String.format("values=[citizenId:%d date:%s amount:%d clientId:%d iban:%s] signature=%s mac=%s", 
    			citizenId, new String(date), amount, clientId, new String(iban), signature, mac);
	}
	
	public Long doDeleteByCitizenId(int citizenId) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(citizenId).array()));
    	String[] redisKeys = lookup(key, 0);

    	return jedis.del(redisKeys);
	}
	
	public Long doDeleteByDate(String date) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(date.getBytes(StandardCharsets.UTF_8)));
    	String[] redisKeys = lookup(key, 1);

    	return jedis.del(redisKeys);
	}
	
	public Long doDeleteByAmount(int amount) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(amount).array()));
    	String[] redisKeys = lookup(key, 2);

    	return jedis.del(redisKeys);
	}
	
	public Long doDeleteByClientId(int clientId) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(ByteBuffer.allocate(Integer.BYTES).putInt(clientId).array()));
    	String[] redisKeys = lookup(key, 3);

    	return jedis.del(redisKeys);
	}
	
	public Long doDeleteByIban(String iban) throws Exception {
		MessageDigest md = MessageDigest.getInstance(config.messageDigestAlgorithm);
		String key = Utils.toHex(md.digest(iban.getBytes(StandardCharsets.UTF_8)));
    	String[] redisKeys = lookup(key, 4);

    	return jedis.del(redisKeys);
	}
	
	public Long doDelete(String key) {
		return jedis.del(key);
	}
	
	private String[] lookup(String key, int column) {
		return lookupTable.entrySet()
                .stream()
                .filter(map -> map.getKey().split(":")[column].equals(key))
                .map(map -> map.getKey())
                .toArray(String[]::new);
	}
	
	public void flush() {
		jedis.flushAll();
	}
	
	public void finish() {
		pool.shutdown();
		jedis.disconnect();
	}

}
