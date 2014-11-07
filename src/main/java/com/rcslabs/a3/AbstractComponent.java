package com.rcslabs.a3;

import com.rcslabs.a3.exception.ComponentLifecycleException;
import com.rcslabs.redis.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 15.05.14.
 */
public abstract class AbstractComponent implements IComponent {

    protected final Logger log;
    protected final String name;

    public AbstractComponent(String name){
        this.log = LoggerFactory.getLogger(getClass());
        this.name = name;
    }

    @Override
    public void init() throws ComponentLifecycleException
    {
        log.info("Init " + name);
    }

    @Override
    public void destroy() throws ComponentLifecycleException {
        log.info("Destroy " + name);
    }

    protected void handleOnMessageException(IMessage message, Throwable e){
        log.error(e.getMessage(), e);
    }
}
