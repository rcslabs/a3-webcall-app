package com.rcslabs.a3.test;

import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sx on 07.05.14.
 */
public class TestMessageBrokerDelegate implements IMessageBrokerDelegate{

    private String channel;
    private List<IMessage> messages;
    public List<IMessage> getMessages() {
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
    public String getChannel() {
        return channel;
    }

    @Override
    public void onMessageReceived(IMessage message) {
        messages.add(message);
    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {

    }
}
