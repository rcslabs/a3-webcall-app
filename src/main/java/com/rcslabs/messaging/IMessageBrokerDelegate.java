package com.rcslabs.messaging;

public interface IMessageBrokerDelegate {
	
	void onMessageReceived(String channel, IMessage message);

    void handleOnMessageException(IMessage message, Throwable e);
}
