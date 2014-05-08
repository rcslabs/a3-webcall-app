package com.rcslabs.a3.messaging;

public interface IMessageBrokerDelegate {

    String getChannel();

	void onMessageReceived(IMessage message);

    void handleOnMessageException(IMessage message, Throwable e);
}
