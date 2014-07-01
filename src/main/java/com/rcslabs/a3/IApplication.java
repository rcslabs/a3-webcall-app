package com.rcslabs.a3;

import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.messaging.IMessage;

/**
 * Created by sx on 15.04.14.
 */
public interface IApplication extends IComponent{

    ISession findSession(String value);

    IConfig getConfig();

    void beforeStartSession(IMessage message) throws Exception;
}
