package com.rcslabs.chat;

import com.rcslabs.a3.auth.*;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBroker;
import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 22.04.14.
 */
public class BaseChatApplication implements IChatApplication {

    protected final static Logger log = LoggerFactory.getLogger(BaseChatApplication.class);

    protected final String messagingChannel;
    protected final IMessageBroker broker;
    protected final IAuthController authController;

    public BaseChatApplication(String messagingChannel, IMessageBroker broker) {
        this.messagingChannel = messagingChannel;
        this.broker = broker;
        this.authController = new ChatAuthController(broker, new InMemorySessionStorage(), 3600);
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public IAuthController getAuthController() {
        return authController;
    }

    @Override
    public IConfig getConfig() {
        return null;
    }

    @Override
    public String getMessagingChannel() {
        return messagingChannel;
    }

    @Override
    public void beforeStartSession(IMessage message) throws Exception {

    }

    @Override
    public void onMessageReceived(String channel, IMessage message)
    {
        try {
            validateMessage(message);
            if(message.getType() instanceof AuthMessage.Type)
                handleAuthMessage((AuthMessage) message);
            else if(message.getType() instanceof ChatMessage.Type)
                handleChatMessage((ChatMessage) message);
            else
                log.warn("Unhandled message " + message.getType());
        } catch (Exception e) {
            handleOnMessageException(message, e);
        }
    }

    @Override
    public void validateMessage(IMessage message) throws InvalidMessageException
    {
        if(message.getType() == AuthMessage.Type.START_SESSION){ return; }

        // all messages except START_SESSION must contains parameter "sessionId"
        // validate it and throws an Exception unsuccessfully

        if(!message.has(MessageProperty.SESSION_ID)){
            throw new InvalidMessageException("Skip the message without sessionId " + message);
        } else {
            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            ISession session = authController.findSession(sessionId);
            if(null == session){
                log.warn("Session for message not found " + message);
            }
        }
    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {
        log.error(e.getMessage(), e);
        if(message.getType() == AuthMessage.Type.START_SESSION){
            authController.onSessionFailed(new CriticalFailedSession(message), "Critical error");
        }
    }

    protected void handleAuthMessage(AuthMessage message) throws Exception{
        switch (message.getType())
        {
            case START_SESSION:
                authController.startSession(new Session(message));
                break;

            case CLOSE_SESSION:
                authController.closeSession((String)message.get("sessionId"));
                break;

            default:
                log.warn("Unhandled message " + message.getType());
        }
    }

    protected void handleChatMessage(ChatMessage message) throws Exception {
        switch (message.getType())
        {
            case JOIN_CHATROOM:
            case UNJOIN_CHATROOM:
            case TEXT_MESSAGE:
            default:
                log.warn("Unhandled message " + message.getType());
        }
    }
}
