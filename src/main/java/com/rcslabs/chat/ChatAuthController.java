package com.rcslabs.chat;

import com.rcslabs.a3.auth.*;
import com.rcslabs.a3.messaging.RedisConnector;

import java.util.UUID;

/**
 * Created by sx on 22.04.14.
 */
public class ChatAuthController extends AbstractAuthController {

    public ChatAuthController(RedisConnector broker, ISessionStorage storage) {
        super("auth:chat", broker, storage);
    }

    @Override
    public void onAuthMessage(AuthMessage message) {

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
