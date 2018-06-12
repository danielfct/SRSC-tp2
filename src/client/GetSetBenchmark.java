package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import redis.clients.jedis.Jedis;

public class GetSetBenchmark {
	
    private static final int TOTAL_OPERATIONS = 100;

    public static void main(String[] args) throws UnknownHostException, IOException {
    	
    	Jedis jedis = new Jedis("localhost", 6379);

        jedis.connect();
        jedis.flushAll();

        long begin = Calendar.getInstance().getTimeInMillis();

        for (int n = 0; n <= 10; n++) {
            String key = "foo" + n;
            jedis.set(key, "bar" + n);
        }
        for (int n = 0; n <= 10; n++) {
            String key = "foo" + n;
            jedis.get(key);
        }

        long elapsed = Calendar.getInstance().getTimeInMillis() - begin;

        jedis.disconnect();
        jedis.close();

        System.out.println(((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " ops");
    }
}