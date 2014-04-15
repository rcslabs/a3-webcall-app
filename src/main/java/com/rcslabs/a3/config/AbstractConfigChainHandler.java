package com.rcslabs.a3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by sx on 06.03.14.
 */
public abstract class AbstractConfigChainHandler implements IConfigChainHandler {

    protected final static Logger log = LoggerFactory.getLogger(AbstractConfigChainHandler.class);

    protected Properties data;
    protected IConfigChainHandler next;

    protected AbstractConfigChainHandler(){
        data = new Properties();
    }

    @Override
    public void setNext(IConfigChainHandler next) {
        this.next = next;
    }

    @Override
    public Boolean getPropertyAsBoolean(String key){
        return Boolean.valueOf(getPropertyAsString(key));
    }

    @Override
    public Integer getPropertyAsInteger(String key){
        return Integer.valueOf(getPropertyAsString(key));
    }

    @Override
    public String getPropertyAsString(String key){
        if(data.containsKey(key)) return data.getProperty(key);
        if(null == next) return null;
        return next.getPropertyAsString(key);
    }
}
