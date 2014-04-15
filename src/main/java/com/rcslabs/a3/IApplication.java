package com.rcslabs.a3;

import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.messaging.IMessage;
import com.rcslabs.webcall.IConfig;

/**
 * Created by sx on 15.04.14.
 */
public interface IApplication {
    boolean ready();

    IAuthController getAuthController();

    IConfig getConfig();

    String getChannelName();

    void beforeStartSession(IMessage message) throws Exception;
}
