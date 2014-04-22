package com.rcslabs.a3.auth;

import com.rcslabs.a3.IDataStorage;
import com.rcslabs.a3.fsm.IFSM;

public interface ISession extends IDataStorage<Object>, IFSM<ISession.State, SessionSignal> {
	
	public enum State{
        INIT, CONNECTING, CONNECTED, FAILED, CLOSED
	}

    public String getService();

	public String getUsername();
	
	public String getPassword();
	
	public String getSessionId();

    public String getClientId();

    public String getSender();
}
