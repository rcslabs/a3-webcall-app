package com.rcslabs.a3.messaging;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class RedisConnector implements IMessageBroker {

	protected final static Logger log = LoggerFactory.getLogger(RedisConnector.class);

    private final List<RedisSubscriberThread> subscribers;
    private final ExecutorService subscribersExecutor;
    private JedisPool pool;

	public RedisConnector(){
        subscribers = new ArrayList<>();
        subscribersExecutor = Executors.newCachedThreadPool();//(6);  // default pool size=8
	}

    /**
     * Achtung!!! This method blocks the running thread until the Redis is not connected.
     * And this method must invoked only once 'at-first-line' on main thread starting.
     */
    public void connect(URI uri)
    {
        // http://commons.apache.org/proper/commons-pool/api-1.6/index.html?org/apache/commons/pool/impl/GenericObjectPool.html
        GenericObjectPool.Config c = new GenericObjectPool.Config();
        c.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        c.timeBetweenEvictionRunsMillis = 600000; // 10 min
        c.minEvictableIdleTimeMillis = 60000;     // 1 min

        pool = new JedisPool(c, uri.getHost(), (-1 == uri.getPort() ? Protocol.DEFAULT_PORT : uri.getPort()));

        do{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { }
        } while (!checkJedisInternal());

        Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(new Runnable() {
                @Override public void run() { checkJedisInternal(); }
            }, 0, 1, TimeUnit.SECONDS);
    }

    public void dispose()
    {
        for(RedisSubscriberThread th : subscribers){
            th.getSubscriber().unsubscribe();
            th.interrupt();
        }

        subscribers.clear();

        if(pool != null){
            try {
                pool.destroy();
            } catch (JedisException e) {}
        }
    }

    public Jedis getResource(){
        Jedis resource = null;
        try{
            resource = pool.getResource();
            return resource;
        } catch(JedisConnectionException e){
            pool.returnBrokenResource(resource);
            return null;
        }
    }

    public void returnResource(Jedis resource){
        if(resource == null){ return; }
        try{
            if(resource.isConnected()){
                pool.returnResource(resource);
            }
        } catch(JedisConnectionException e){
            pool.returnBrokenResource(resource);
        }
    }

    @Override
	public void publish(String channel, IMessage message){
        Jedis j = getResource();
        if(j != null){
            j.publish(channel, MessageMarshaller.getInstance().toJson(message));
            log.info("sent " + channel + " " + message);
            returnResource(j);
        }else{
            log.warn("Publish message on disconnected Redis");
        }
	}

    @Override
	public void subscribe(final IMessageBrokerDelegate delegate) {
		log.info("Subscribe to " + delegate.getChannel());
        RedisSubscriber sub = new RedisSubscriber(delegate);
        RedisSubscriberThread th = new RedisSubscriberThread(pool, sub, log);
        subscribers.add(th);
        subscribersExecutor.submit(th);
    }

    @Override
	public void unubscribe(IMessageBrokerDelegate delegate) {
        log.info("Unsubscribe from " + delegate.getChannel());
        for(RedisSubscriberThread th : subscribers){
            RedisSubscriber subscriber = th.getSubscriber();
            if(subscriber.getChannel().equals(delegate.getChannel())){
                subscriber.unsubscribe();
                th.interrupt();
                subscribers.remove(th);
                break;
            }
        }
	}

    private boolean checkJedisInternal() {
        boolean result = false;
        Jedis j = null;
        try{
            j = getResource();
            if(j != null){ j.ping(); }
            subscribeAllInternal();
            result = true;
        } catch(JedisConnectionException e){
            returnResource(j);
        }

        if(!result){
            log.warn("Redis not connected");
        }
        return result;
    }

    private void subscribeAllInternal(){
        for(RedisSubscriberThread th : subscribers){
            if(!th.getSubscriber().isDirty()) continue;
            subscribersExecutor.submit(th);
        }
    }
}
