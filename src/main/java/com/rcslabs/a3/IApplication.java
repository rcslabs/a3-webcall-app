package com.rcslabs.a3;

import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.webcall.ICallAppConfig;

/**
 * Created by sx on 15.04.14.
 */
public interface IApplication {

    boolean ready();

    IAuthController getAuthController();

    ICallAppConfig getConfig();

    String getMessagingChannel();

    void beforeStartSession(IMessage message) throws Exception;
}
