package com.rcslabs.a3.messaging;

import com.rcslabs.redis.RedisConnector;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.net.URI;

/**
 * Created by ykrkn on 07.11.14.
 */
public class RedisFactory {

    private final JedisPool pool;
    private final RedisConnector conn;

    public RedisFactory(URI redisUri){
        pool = new JedisPool(redisUri.getHost(), (-1 == redisUri.getPort() ? Protocol.DEFAULT_PORT : redisUri.getPort()));
        conn = new RedisConnector(pool);
    }

    public void init() throws Exception {
        conn.subscribe();
    }

    public void dispose() {
        conn.dispose();
        pool.close();
    }

    public RedisConnector getConnector(){
        return conn;
    }
}
