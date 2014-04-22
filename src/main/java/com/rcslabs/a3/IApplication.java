package com.rcslabs.a3;

import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;

/**
 * Created by sx on 15.04.14.
 */
public interface IApplication extends IMessageBrokerDelegate {

    boolean ready();

    IAuthController getAuthController();

    IConfig getConfig();

    String getMessagingChannel();

    void beforeStartSession(IMessage message) throws Exception;
}
