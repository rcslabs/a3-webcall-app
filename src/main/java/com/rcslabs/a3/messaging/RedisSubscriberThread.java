package com.rcslabs.a3.messaging;

import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by sx on 16.05.14.
 */
public class RedisSubscriberThread extends Thread {

    private final JedisPool pool;
    private final RedisSubscriber subscriber;
    private final Logger log;

    public RedisSubscriberThread(JedisPool pool, RedisSubscriber subscriber, Logger log){
        super("RedisSubscriberThread-"+subscriber.getChannel());
        this.pool = pool;
        this.subscriber = subscriber;
        this.log = log;
    }

    public RedisSubscriber getSubscriber(){
        return subscriber;
    }

    @Override
    public void run() {
        Jedis jedisSub = null;
        try {
            jedisSub = pool.getResource();
            subscriber.setDirty(false);
            jedisSub.subscribe(subscriber, subscriber.getChannel());
        } catch (JedisConnectionException e) {
            log.error("Redis connection failed");
            subscriber.setDirty(true);
            if (jedisSub != null) {
                pool.returnBrokenResource(jedisSub);
            }
        }
    }
}
