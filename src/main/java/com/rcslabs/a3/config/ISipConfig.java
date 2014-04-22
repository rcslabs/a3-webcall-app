package com.rcslabs.a3.config;

/**
 * Created by sx on 22.04.14.
 */
public interface ISipConfig {
    public abstract String getSipLocalHost();
    public abstract Integer getSipLocalPort();
    public abstract String getSipServerHost();
    public abstract Integer getSipServerPort();
    public abstract String getSipProxyHost();
    public abstract Integer getSipProxyPort();
    public abstract Integer getSipExpires();
    public abstract String getSipUserAgent();
}
