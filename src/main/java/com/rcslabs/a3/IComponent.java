package com.rcslabs.a3;

import com.rcslabs.a3.exception.ComponentLifecycleException;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;

/**
 * Created by sx on 15.05.14.
 */
public interface IComponent extends IMessageBrokerDelegate {

    public void init() throws ComponentLifecycleException;

    public void destroy() throws ComponentLifecycleException;
}
