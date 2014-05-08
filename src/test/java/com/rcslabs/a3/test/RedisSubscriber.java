package com.rcslabs.a3.test;

import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sx on 07.05.14.
 */
public class RedisSubscriber implements IMessageBrokerDelegate{

    private List<IMessage> messages;

    public List<IMessage> getMessages() {
        return messages;
    }

    public RedisSubscriber(){
        messages = new ArrayList<>();
    }

    public void clean(){
        messages.clear();
    }

    @Override
    public String getChannel() {
        return "test";
    }

    @Override
    public void onMessageReceived(IMessage message) {
        System.out.println(message);
        messages.add(message);
    }

    @Override
    public void validateMessage(IMessage message) throws InvalidMessageException {

    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {

    }
}
