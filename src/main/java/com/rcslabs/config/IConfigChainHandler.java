package com.rcslabs.config;

/**
 * Created by sx on 06.03.14.
 */
public interface IConfigChainHandler {
    public abstract void setNext(IConfigChainHandler next);
    public abstract Boolean getPropertyAsBoolean(String key);
    public abstract Integer getPropertyAsInteger(String key);
    public abstract String getPropertyAsString(String key);
}
