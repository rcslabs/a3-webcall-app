package com.rcslabs.auth;


import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.fsm.AbstractFSM;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.webcall.MessageProperty;

public class CriticalFailedSession extends AbstractFSM<ISession.State, SessionSignal> implements ISession{

    private final String service;
    private final String sender;
    private final String clientId;
    private final String sessionId;

    public CriticalFailedSession(IMessage message) {
        service = (String)message.get(MessageProperty.SERVICE);
        sender = (String)message.get(MessageProperty.SENDER);
        clientId = (String)message.get(MessageProperty.CLIENT_ID);
        sessionId = (message.has(MessageProperty.SESSION_ID) ? (String)message.get(MessageProperty.SESSION_ID) : null);
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
    public boolean has(String key) {
        return false;
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public void onEvent(SessionSignal event) { /* do nothing */ }
}
