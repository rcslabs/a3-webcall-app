package com.rcslabs.a3.messaging;

import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by sx on 16.05.14.
 */
public class RedisSubscriberThread extends Thread {

    private final RedisConnector redisConnector;
    private final RedisSubscriber subscriber;
    private final Logger log;

    public RedisSubscriberThread(RedisConnector redisConnector, IMessageBrokerDelegate delegate, Logger log){
        super("RedisSubscriberThread-"+delegate.getChannel());
        this.redisConnector = redisConnector;
        this.subscriber = new RedisSubscriber(delegate);
        this.log = log;
    }

    public RedisSubscriber getSubscriber(){
        return subscriber;
    }

    @Override
    public void run() {
        Jedis j = null;
        try {
            j = redisConnector.getResource();
            subscriber.setDirty(false);
            j.subscribe(subscriber, subscriber.getChannel());
        } catch (JedisConnectionException e) {
            log.error("Redis connection failed");
            subscriber.setDirty(true);
        } finally {
            redisConnector.returnResource(j);
        }
    }
}
