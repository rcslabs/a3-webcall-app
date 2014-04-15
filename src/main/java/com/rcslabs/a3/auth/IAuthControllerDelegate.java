package com.rcslabs.a3.auth;

public interface IAuthControllerDelegate {

	void onSessionStarted(ISession session);
	
	void onSessionFailed(ISession session, String reason);
	
	void onSessionClosed(ISession session);
}
