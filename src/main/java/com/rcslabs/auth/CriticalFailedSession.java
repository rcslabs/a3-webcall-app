package com.rcslabs.auth;


import com.rcslabs.fsm.AbstractFSM;
import com.rcslabs.webcall.AlenaMessage;

public class CriticalFailedSession extends AbstractFSM<ISession.State, SessionEvent> implements ISession{

    private final String service;
    private final String sender;
    private final String clientId;
    private final String sessionId;

    public CriticalFailedSession(AlenaMessage message) {
        service = (String)message.get(AlenaMessage.PROP_SERVICE);
        sender = (String)message.get(AlenaMessage.PROP_SENDER);
        clientId = (String)message.get(AlenaMessage.PROP_CLIENT_ID);
        sessionId = (message.has(AlenaMessage.PROP_SESSION_ID) ? (String)message.get(AlenaMessage.PROP_SESSION_ID) : null);
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getSessionId() {
        return sessionId;
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
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void onEvent(SessionEvent event) { /* do nothing */ }
}
