package com.rcslabs.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RedisMessageBroker implements IMessageBroker {

	protected final static Logger log = LoggerFactory.getLogger(RedisMessageBroker.class);
	
	private Map<String, RedisSubscriber> subscribers;
	private JedisPool pool;
    private Jedis jedis;
    private ScheduledExecutorService checkSubscriberThreadsSheduler;
    private boolean connected;

	public RedisMessageBroker(){
		this("localhost", Protocol.DEFAULT_PORT);
	}

	public RedisMessageBroker(String host){
		this(host, Protocol.DEFAULT_PORT);
	}

	public RedisMessageBroker(String host, int port){
		super();
        pool = new JedisPool(host, port);
        checkSubscriberThreadsSheduler = Executors.newScheduledThreadPool(1);
        checkSubscriberThreadsSheduler.scheduleAtFixedRate(new CheckSubscribersTimerTask(), 0, 1000, TimeUnit.MILLISECONDS);
        subscribers = new ConcurrentHashMap<String, RedisSubscriber>();
	}

    class CheckSubscribersTimerTask implements Runnable {
        @Override
        public void run() {
            try{
                if(null == jedis){
                    jedis = pool.getResource();
                }
                jedis.ping();

                for(RedisSubscriber s : subscribers.values()){
                    s.checkAndStart();
                }
                if(!connected){
                    log.info("Redis connected");
                }
                connected = true;
            }catch(Exception e){
                pool.returnBrokenResource(jedis);
                connected = false;
                log.error("No connection with redis");
            }
        }
    }

    @Override
	public void publish(String channel, IMessage message){
        if(!connected){ return; }
        jedis.publish(channel, Message.toJson(message));
        log.info("sent " + channel + " " + message);
	}

    @Override
	public void subscribe(final String channel, IMessageBrokerDelegate delegate) {
		log.info("Subscribe to " + channel);
        subscribers.put(channel, new RedisSubscriber(pool, channel, delegate));
	}

    @Override
	public void unubscribe(String channel) {
        RedisSubscriber subscriber = subscribers.remove(channel);
		subscriber.dispose();
	}
}
