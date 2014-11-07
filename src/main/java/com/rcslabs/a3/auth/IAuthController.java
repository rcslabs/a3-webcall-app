package com.rcslabs.a3.auth;

import com.rcslabs.a3.messaging.AuthMessage;

public interface IAuthController {

    void onAuthMessage(AuthMessage message);

	void startSession(ISession session);
	
	void closeSession(String sessionId);

	ISession findSession(String value);

    void onSessionStarted(ISession session);

    void onSessionFailed(ISession session, String reason);

    void onSessionClosed(ISession session);
}
