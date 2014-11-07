package com.rcslabs.a3;

import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.redis.IMessageListener;

/**
 * Created by sx on 15.04.14.
 */
public interface IApplication extends IMessageListener, IComponent {

    ISession findSession(String value);

    IConfig getConfig();

    void beforeStartSession(IAlenaMessage message) throws Exception;
}
