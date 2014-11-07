package com.rcslabs.a3.test;

import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.redis.IMessage;
import com.rcslabs.redis.IMessageListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sx on 07.05.14.
 */
public class TestMessageBrokerDelegate implements IMessageListener {

    private String channel;
    private List<IAlenaMessage> messages;
    public List<IAlenaMessage> getMessages() {
        return messages;
    }

    public TestMessageBrokerDelegate(String channel){
        messages = new ArrayList<>();
        this.channel = channel;
    }

    public void clean(){
        messages.clear();
    }

    @Override
    public void onMessage(String channel, IMessage message) {
        messages.add((IAlenaMessage) message);
    }
}
