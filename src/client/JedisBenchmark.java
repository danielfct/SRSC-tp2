package client;

import java.security.SecureRandom;
import java.util.Calendar;

public class JedisBenchmark {

	// colocar valores no redis
	private static void populateRedis(RedisClient client) throws Exception {
		System.out.println("Populating redis with " + RedisData.DATA_SIZE + " entries");
		for (int i = 0; i < RedisData.DATA_SIZE; i++) {
			int citizenId = RedisData.citizenCardId[i];
			String date = RedisData.date[i];
			int amount = RedisData.amount[i];
			int clientId = RedisData.clientId[i];
			String iban = RedisData.iban[i];
			client.doSet(citizenId, date, amount, clientId, iban);
		}
		client.flush();
	}

	// benchmark sem atestacao, efetua operacoes SET/GET/DELETE aleatoriamente
	private static void operationsWithoutAttestation(int nOperations, RedisClient client) throws Exception {
		System.out.println("Doing " + nOperations + " random operations on redis without attestation");
		int numSets = 0;
		int numGets = 0;
		int numDeletes = 0;
		SecureRandom r = new SecureRandom();
	    long begin = Calendar.getInstance().getTimeInMillis();
		for (int i = 0; i < nOperations; i++) {
			double operation = r.nextDouble();
			int dataIndex = r.nextInt(RedisData.DATA_SIZE);
			if (operation < .33) {
				int citizenId = RedisData.citizenCardId[dataIndex];
				String date = RedisData.date[dataIndex];
				int amount = RedisData.amount[dataIndex];
				int clientId = RedisData.clientId[dataIndex];
				String iban = RedisData.iban[dataIndex];
				client.doSet(citizenId, date, amount, clientId, iban);
				numSets++;
			} else if (operation < .66) {
				int column = r.nextInt(RedisData.NUM_COLUMNS);
				if (column == 0)
					client.doGetByCitizenId(RedisData.citizenCardId[dataIndex]);
				else if (column == 1)
					client.doGetByDate(RedisData.date[dataIndex]);
				else if (column == 2)
					client.doGetByAmount(RedisData.amount[dataIndex]);
				else if (column == 3)
					client.doGetByClientId(RedisData.clientId[dataIndex]);
				else
					client.doGetByIban(RedisData.iban[dataIndex]);
				numGets++;
			} else {
				int column = r.nextInt(RedisData.NUM_COLUMNS);
				if (column == 0)
					client.doDeleteByCitizenId(RedisData.citizenCardId[dataIndex]);
				else if (column == 1)
					client.doDeleteByDate(RedisData.date[dataIndex]);
				else if (column == 2)
					client.doDeleteByAmount(RedisData.amount[dataIndex]);
				else if (column == 3)
					client.doDeleteByClientId(RedisData.clientId[dataIndex]);
				else
					client.doDeleteByIban(RedisData.iban[dataIndex]);
				numDeletes++;
			}
		}
		long elapsed = Calendar.getInstance().getTimeInMillis() - begin;
		System.out.printf("%d sets, %d gets, %d deletes with %d ops/s\n", 
				numSets, numGets, numDeletes, (1000 * nOperations) / elapsed);
	}
	
	// benchmark com atestacao, efetua operacoes SET/GET/DELETE aleatoriamente apos atestacao do servidor
	private static void operationsWithAttestation(int nOperations, RedisClient client) throws Exception {
		System.out.println("Doing " + nOperations + " random operations on redis with attestation");
		int numSets = 0;
		int numGets = 0;
		int numDeletes = 0;
		SecureRandom r = new SecureRandom();
	    long begin = Calendar.getInstance().getTimeInMillis();
		for (int i = 0; i < nOperations; i++) {
			client.doAttestation();
			double operation = r.nextDouble();
			int dataIndex = r.nextInt(RedisData.DATA_SIZE);
			if (operation < .33) {
				int citizenId = RedisData.citizenCardId[dataIndex];
				String date = RedisData.date[dataIndex];
				int amount = RedisData.amount[dataIndex];
				int clientId = RedisData.clientId[dataIndex];
				String iban = RedisData.iban[dataIndex];
				client.doSet(citizenId, date, amount, clientId, iban);
				numSets++;
			} else if (operation < .66) {
				int column = r.nextInt(RedisData.NUM_COLUMNS);
				if (column == 0)
					client.doGetByCitizenId(RedisData.citizenCardId[dataIndex]);
				else if (column == 1)
					client.doGetByDate(RedisData.date[dataIndex]);
				else if (column == 2)
					client.doGetByAmount(RedisData.amount[dataIndex]);
				else if (column == 3)
					client.doGetByClientId(RedisData.clientId[dataIndex]);
				else
					client.doGetByIban(RedisData.iban[dataIndex]);
				numGets++;
			} else {
				int column = r.nextInt(RedisData.NUM_COLUMNS);
				if (column == 0)
					client.doDeleteByCitizenId(RedisData.citizenCardId[dataIndex]);
				else if (column == 1)
					client.doDeleteByDate(RedisData.date[dataIndex]);
				else if (column == 2)
					client.doDeleteByAmount(RedisData.amount[dataIndex]);
				else if (column == 3)
					client.doDeleteByClientId(RedisData.clientId[dataIndex]);
				else
					client.doDeleteByIban(RedisData.iban[dataIndex]);
				numDeletes++;
			}
		}
		long elapsed = Calendar.getInstance().getTimeInMillis() - begin;
		System.out.printf("%d sets, %d gets, %d deletes with %d ops/s\n", 
				numSets, numGets, numDeletes, (1000 * nOperations) / elapsed);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: java -Djavax.net.ssl.trustStore=clienttruststore "
					+ "JedisBenchmark configFile numOperations");
			System.exit(0);
		}
		String configFile = args[0];
		int numOperations = Integer.valueOf(args[1]);
		
		ClientConfiguration config = new ClientConfiguration(configFile);
		RedisClient client = new RedisClient(config);
		populateRedis(client);
		operationsWithoutAttestation(numOperations, client);
		operationsWithAttestation(numOperations, client);
		client.finish();
	}

}
