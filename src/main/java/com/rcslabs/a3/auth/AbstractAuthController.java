package com.rcslabs.a3.auth;

import com.rcslabs.a3.AbstractController;
import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.RedisConnector;
import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 22.04.14.
 */
public abstract class AbstractAuthController extends AbstractController implements IAuthController {

    protected final static Logger log = LoggerFactory.getLogger(AbstractAuthController.class);
    protected final ISessionStorage storage;

    public AbstractAuthController(String channel, RedisConnector redisConnector, ISessionStorage storage){
        super(channel, redisConnector);
        this.storage = storage;
    }

    @Override
    public void onMessageReceived(IMessage message) {
        try{
            if(!(message instanceof AuthMessage)){
                throw new InvalidMessageException("Expected AuthMessage");
            }
            onAuthMessage((AuthMessage)message);
        } catch (Exception e){
            handleOnMessageException(message, e);
        }
    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {
        log.error(e.getMessage(), e);
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
        IMessage message = new AuthMessage(AuthMessage.Type.SESSION_STARTED);
        message.set(MessageProperty.SERVICE, session.getService());
        message.set(MessageProperty.SESSION_ID, session.getSessionId());
        message.set(MessageProperty.CLIENT_ID, session.getClientId());
        redisConnector.publish(session.getSender(), message);
    }

    @Override
    public void onSessionFailed(ISession session, String reason) {
        log.info("onSessionFailed " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_FAILED));
        IMessage message = new AuthMessage(AuthMessage.Type.SESSION_FAILED);
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
        IMessage message = new AuthMessage(AuthMessage.Type.SESSION_CLOSED);
        message.set(MessageProperty.SERVICE, session.getService());
        message.set(MessageProperty.SESSION_ID, session.getSessionId());
        message.set(MessageProperty.CLIENT_ID, session.getClientId());
        redisConnector.publish(session.getSender(), message);
    }
}
