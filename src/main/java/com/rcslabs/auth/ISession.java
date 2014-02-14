package com.rcslabs.auth;

import com.rcslabs.fsm.IFSM;

public interface ISession  extends IFSM<ISession.State, SessionEvent> {
	
	public enum State{
        INIT, CONNECTING, CONNECTED, FAILED, CLOSED
	}

    public String getService();

	public String getUsername();
	
	public String getPassword();
	
	public String getSessionId();

    public String getClientId();

    public String getSender();

	public void set(String key, Object value);
	
	public Object get(String key);
}
