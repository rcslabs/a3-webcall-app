package com.rcslabs.a3.auth;

import com.rcslabs.a3.IDataStorage;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;


public interface IAuthController {

	void setTimeToLive(int value);
	
	int getTimeToLive();
	
	void startSession(ISession session);
	
	void closeSession(String sessionId);

	ISession findSession(String value);

    void onSessionStarted(ISession session);

    void onSessionFailed(ISession session, String reason);

    void onSessionClosed(ISession session);
}
