package com.rcslabs.chat;

import com.rcslabs.a3.auth.AbstractAuthController;
import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.auth.ISessionStorage;
import com.rcslabs.a3.messaging.IMessageBroker;

/**
 * Created by sx on 22.04.14.
 */
public class ChatAuthController extends AbstractAuthController {

    public ChatAuthController(IMessageBroker broker, ISessionStorage storage) {
        super(broker, storage);
    }

    @Override
    public void startSession(ISession session) {
        storage.set(session.getSessionId(), session);
    }

    @Override
    public void closeSession(String sessionId) {
        storage.delete(sessionId);
    }
}
