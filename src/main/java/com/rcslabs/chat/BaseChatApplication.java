package com.rcslabs.chat;

import com.rcslabs.a3.auth.AuthMessage;
import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.a3.auth.InMemorySessionStorage;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBroker;
import com.rcslabs.webcall.ICallAppConfig;
import com.rcslabs.a3.auth.CriticalFailedSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 22.04.14.
 */
public class BaseChatApplication implements IChatApplication {

    protected final static Logger log = LoggerFactory.getLogger(BaseChatApplication.class);

    protected final String messagingChannel;
    protected final ICallAppConfig config;
    protected final IMessageBroker broker;
    protected final IAuthController authController;

    public BaseChatApplication(String messagingChannel, ICallAppConfig config, IMessageBroker broker) {
        this.messagingChannel = messagingChannel;
        this.config = config;
        this.broker = broker;
        this.authController = new ChatAuthController(broker, new InMemorySessionStorage());
        this.authController.setTimeToLive(3600);
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
        return config;
    }

    @Override
    public String getMessagingChannel() {
        return messagingChannel;
    }

    @Override
    public void beforeStartSession(IMessage message) throws Exception {

    }

    @Override
    public void onMessageReceived(String channel, IMessage message) {

    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {
        log.error(e.getMessage(), e);

        if(message.getType() == AuthMessage.Type.START_SESSION){
            authController.onSessionFailed(new CriticalFailedSession(message), "Critical error");
        }
    }
}
