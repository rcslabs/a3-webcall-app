package com.rcslabs.a3.messaging;

public interface IMessageBroker {

	public abstract void publish(String channel, IMessage message);

	public abstract void subscribe(IMessageBrokerDelegate delegate);
	
	public abstract void unubscribe(IMessageBrokerDelegate delegate);
}