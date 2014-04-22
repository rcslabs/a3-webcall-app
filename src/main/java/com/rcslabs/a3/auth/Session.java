package com.rcslabs.a3.auth;

import com.rcslabs.a3.fsm.AbstractFSM;

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

    public Session(AuthMessage message) {
        this((String) message.get("service"),
             (String) message.get("username"),
             (String) message.get("password"),
             (String) message.get("clientId"),
             (String) message.get("sender"));
    }

	public Session(String service, String username, String password, String clientId, String sender){
        this.service = service;
		this.username = username;
		this.password = password;
        this.clientId = clientId;
        this.sender = sender;
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
                if(event.getType() == AuthMessage.Type.START_SESSION){
                    setState(State.CONNECTING, event);
                }
                break;

            case CONNECTING:
                if(event.getType() == AuthMessage.Type.SESSION_STARTED){
                    setState(State.CONNECTED, event);
                } else if(event.getType() == AuthMessage.Type.SESSION_FAILED){
                    setState(State.FAILED, event);
                } else {
                    unhandledEvent(event);
                }
                break;
            case CONNECTED:
                if(event.getType() == AuthMessage.Type.SESSION_FAILED){
                    setState(State.FAILED, event);
                } else if(event.getType() == AuthMessage.Type.SESSION_CLOSED){
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

    @Override
	public String getSender() {
		return sender;
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
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    public void delete(String key) {
        if(data.containsKey(key)){
            data.remove(key);
        }
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
