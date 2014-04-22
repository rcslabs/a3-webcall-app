package com.rcslabs.a3.messaging;

import com.rcslabs.a3.exception.InvalidMessageException;

public interface IMessageBrokerDelegate {
	
	void onMessageReceived(String channel, IMessage message);

    void validateMessage(IMessage message) throws InvalidMessageException;

    void handleOnMessageException(IMessage message, Throwable e);
}
