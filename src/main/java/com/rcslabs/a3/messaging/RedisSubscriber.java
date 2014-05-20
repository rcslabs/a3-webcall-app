package com.rcslabs.a3.messaging;

import redis.clients.jedis.JedisPubSub;

/**
 * Created by sx on 16.05.14.
 */
public class RedisSubscriber extends JedisPubSub {

    private final IMessageBrokerDelegate delegate;
    private boolean dirty;

    public RedisSubscriber(IMessageBrokerDelegate delegate) {
        this.delegate = delegate;
        this.dirty = false;
    }

    public void setDirty(boolean value){
        this.dirty = value;
    }

    public boolean isDirty(){
        return dirty;
    }

    public String getChannel(){
        return delegate.getChannel();
    }

    @Override
    public void onMessage(String channel, String message) {
        IMessage m = MessageMarshaller.getInstance().fromJson(message);
        delegate.onMessageReceived(m);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {

    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {

    }
}
