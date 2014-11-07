package com.rcslabs.a3;

import com.rcslabs.a3.exception.ComponentLifecycleException;

/**
 * Created by sx on 15.05.14.
 */
public interface IComponent {

    public void init() throws ComponentLifecycleException;

    public void destroy() throws ComponentLifecycleException;
}
