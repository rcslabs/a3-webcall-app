package com.rcslabs.a3.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RedisConnector implements IMessageBroker {

	protected final static Logger log = LoggerFactory.getLogger(RedisConnector.class);
	
	private Map<String, RedisSubscriber> subscribers;
	private JedisPool pool;
    private Jedis jedis;
    private ScheduledExecutorService checkSubscriberThreadsSheduler;
    private boolean connected;

	public RedisConnector(URI uri){
		super();
        pool = new JedisPool(uri.getHost(), (-1 == uri.getPort() ? Protocol.DEFAULT_PORT : uri.getPort()));
        checkSubscriberThreadsSheduler = Executors.newScheduledThreadPool(1);
        checkSubscriberThreadsSheduler.scheduleAtFixedRate(new CheckSubscribersTimerTask(), 0, 1000, TimeUnit.MILLISECONDS);
        subscribers = new ConcurrentHashMap<>();
	}

    public Jedis getResource(){
        return pool.getResource();
    }

    public void returnResource(Jedis resource){
        pool.returnResource(resource);
    }

    public void returnBrokenResource(Jedis resource){
        pool.returnBrokenResource(resource);
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
        jedis.publish(channel, MessageMarshaller.getInstance().toJson(message));
        log.info("sent " + channel + " " + message);
	}

    @Override
	public void subscribe(IMessageBrokerDelegate delegate) {
		log.info("Subscribe to " + delegate.getChannel());
        RedisSubscriber rs = new RedisSubscriber(pool, delegate);
        subscribers.put(delegate.getChannel(), rs);
        rs.checkAndStart();
	}

    @Override
	public void unubscribe(IMessageBrokerDelegate delegate) {
        RedisSubscriber subscriber = subscribers.remove(delegate.getChannel());
		subscriber.dispose();
	}
}
