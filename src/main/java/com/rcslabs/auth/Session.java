package com.rcslabs.auth;

import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.fsm.AbstractFSM;
import com.rcslabs.webcall.MessageType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Session extends AbstractFSM<ISession.State, SessionSignal> implements ISession{
		
	private final String username;
	private final String password;
    private final String service;

	private String sessionId;
	private String clientId;
	private String sender;
	private String uri;
	private long stime;
	private Map<String, Object> data;
	private ISession.State state;
	
	public Session(String service, String username, String password){
        this.service = service;
		this.username = username;
		this.password = password;
		stime = new Date().getTime();
		data = new HashMap<String, Object>();
		state = State.INIT;
	}

    @Override
    public String getService() {
        return service;
    }

    @Override
	public String getUsername() {
		return username;
	}

    @Override
	public String getPassword() {
		return password;
	}

    @Override
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

    @Override
    public void onEvent(SessionSignal event) {
        switch(this.state) {
            case INIT:
                if(event.getType() == MessageType.START_SESSION){
                    setState(State.CONNECTING, event);
                }
                break;

            case CONNECTING:
                if(event.getType() == MessageType.SESSION_STARTED){
                    setState(State.CONNECTED, event);
                } else if(event.getType() == MessageType.SESSION_FAILED){
                    setState(State.FAILED, event);
                } else {
                    unhandledEvent(event);
                }
                break;
            case CONNECTED:
                if(event.getType() == MessageType.SESSION_FAILED){
                    setState(State.FAILED, event);
                } else if(event.getType() == MessageType.SESSION_CLOSED){
                    setState(State.CLOSED, event);
                } else {
                    unhandledEvent(event);
                }
                break;
            case FAILED:
            case CLOSED:
            default:
                unhandledEvent(event);
        }
    }

    @Override
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

    @Override
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

    @Override
    public void set(String key, Object value) {
        data.put(key, value);
    }

    @Override
    public Object get(String key) {
        if(data.containsKey(key)){
            return data.get(key);
        }
        return null;
    }

	@Override
	public String toString() {
		return "Session [username=" + username	
				+ ", sender=" + sender				
				+ ", sessionId=" + sessionId
				+ ", clientId=" + clientId
				+ ", state=" + state
				+ ", date=" + new Date(stime).toString() 
				+ ", uri=" + uri + "]";
	}
}
