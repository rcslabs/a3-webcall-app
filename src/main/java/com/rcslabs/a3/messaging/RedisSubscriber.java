package com.rcslabs.a3.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

class RedisSubscriber extends JedisPubSub implements Runnable {

    protected final static Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

    private String channelName;
	private IMessageBrokerDelegate delegate;
    private JedisPool pool;
    private Jedis jedis;
    private Thread thread;

    public RedisSubscriber(JedisPool pool, String channelName, IMessageBrokerDelegate delegate){
        super();
        this.pool = pool;
        this.channelName = channelName;
		this.delegate = delegate;
	}

    public void checkAndStart() {
        if(null == thread || thread.getState() == Thread.State.TERMINATED){
            thread = new Thread(this, "RedisSubscriber-" + channelName);
            thread.start();
        }
    }

    @Override
    public void run() {
        try{
            jedis = pool.getResource();
            if(null != jedis){
                jedis.subscribe(this, channelName);
            }
        } catch(Exception e){ /* do nothing */ }

        dispose();
    }

    public void dispose(){
        if(null != jedis){
            jedis.disconnect();
            pool.returnResource(jedis);
        }
    }

	@Override
	public void onMessage(String channel, String message) {
        try{
            log.info("recv channel=" + channel + ", message=" + message);
		    IMessage m = MessageMarshaller.getInstance().fromJson(message);
            delegate.onMessageReceived(channel, m);
        } catch(Exception e){
            log.error("Error on message processing " + message, e);
        }
	}

	@Override public void onUnsubscribe(String channel, int subscribedChannels) {}			
	@Override public void onSubscribe(String channel, int subscribedChannels) {}			
	@Override public void onPUnsubscribe(String pattern, int subscribedChannels) {}			
	@Override public void onPSubscribe(String pattern, int subscribedChannels) {}
	@Override public void onPMessage(String pattern, String channel, String message) {}

}
