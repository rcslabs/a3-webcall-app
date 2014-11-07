package com.rcslabs.a3.auth;

import com.rcslabs.a3.AbstractComponent;
import com.rcslabs.a3.IComponent;
import com.rcslabs.a3.IDataStorage;
import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.AuthMessage;
import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.a3.messaging.MessageProperty;
import com.ykrkn.redis.IMessage;
import com.ykrkn.redis.RedisConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 22.04.14.
 */
public abstract class AbstractAuthController extends AbstractComponent implements IAuthController, IComponent {

    protected final static Logger log = LoggerFactory.getLogger(AbstractAuthController.class);
    protected final IDataStorage<ISession> storage;
    protected final RedisConnector redisConnector;

    public AbstractAuthController(String name, RedisConnector redisConnector, IDataStorage<ISession> storage){
        super(name);
        this.redisConnector = redisConnector;
        this.storage = storage;
    }

    @Override
    public ISession findSession(String value) {
        if(!storage.has(value)) return null;
        return storage.get(value);
    }

    @Override
    public abstract void startSession(ISession session);

    @Override
    public abstract void closeSession(String sessionId);

    @Override
    public void onSessionStarted(ISession session) {
        log.info("onSessionStarted " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_STARTED));
        IAlenaMessage message = new AuthMessage(AuthMessage.Type.SESSION_STARTED);
        message.set(MessageProperty.SERVICE, session.getService());
        message.set(MessageProperty.SESSION_ID, session.getSessionId());
        message.set(MessageProperty.CLIENT_ID, session.getClientId());
        redisConnector.publish(session.getSender(), message);
    }

    @Override
    public void onSessionFailed(ISession session, String reason) {
        log.info("onSessionFailed " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_FAILED));
        IAlenaMessage message = new AuthMessage(AuthMessage.Type.SESSION_FAILED);
        message.set(MessageProperty.SERVICE, session.getService());
        message.set(MessageProperty.SESSION_ID, session.getSessionId());
        message.set(MessageProperty.CLIENT_ID, session.getClientId());
        message.set(MessageProperty.REASON, reason);
        redisConnector.publish(session.getSender(), message);
    }

    @Override
    public void onSessionClosed(ISession session) {
        log.info("onSessionClosed " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_CLOSED));
        IAlenaMessage message = new AuthMessage(AuthMessage.Type.SESSION_CLOSED);
        message.set(MessageProperty.SERVICE, session.getService());
        message.set(MessageProperty.SESSION_ID, session.getSessionId());
        message.set(MessageProperty.CLIENT_ID, session.getClientId());
        redisConnector.publish(session.getSender(), message);
    }
}
