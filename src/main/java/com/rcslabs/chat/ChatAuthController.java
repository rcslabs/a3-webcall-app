package com.rcslabs.chat;

import com.rcslabs.a3.auth.AbstractAuthController;
import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.auth.ISessionStorage;
import com.rcslabs.a3.auth.Session;
import com.rcslabs.a3.messaging.IMessageBroker;

import java.util.UUID;

/**
 * Created by sx on 22.04.14.
 */
public class ChatAuthController extends AbstractAuthController {

    public ChatAuthController(IMessageBroker broker, ISessionStorage storage, int timeToLive) {
        super(broker, storage, timeToLive);
    }

    @Override
    public void startSession(ISession session) {
        ((Session)session).setSessionId(UUID.randomUUID().toString());
        storage.set(session.getSessionId(), session);
        super.onSessionStarted(session);
    }

    @Override
    public void closeSession(String sessionId) {
        ISession session = storage.get(sessionId);
        if(null == session) return;
        storage.delete(sessionId);
        super.onSessionClosed(session);
    }
}
