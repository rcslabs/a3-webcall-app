package com.rcslabs.auth;


import com.rcslabs.redis.IMessageBrokerDelegate;

public interface IAuthController extends IMessageBrokerDelegate {

	void setTimeToLive(int value);
	
	int getTimeToLive();
	
	void startSession(ISession session);
	
	void closeSession(String sessionId);
	
	ISession findSession(String value);
}
