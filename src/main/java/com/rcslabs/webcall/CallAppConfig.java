package com.rcslabs.webcall;

import com.rcslabs.a3.config.AbstractConfig;

public class CallAppConfig extends AbstractConfig implements ICallAppConfig {

    @Override
    public String getDatabaseUrl() {
        return getPropertyAsString("db-url");
    }

    @Override
    public String getDatabaseUser() {
        return getPropertyAsString("db-user");
    }

    @Override
    public String getDatabasePassword() {
        return getPropertyAsString("db-password");
    }

    @Override
    public String getSipLocalHost() {
        return getPropertyAsString("sip-local-host");
    }

    @Override
    public Integer getSipLocalPort() {
        return getPropertyAsInteger("sip-local-port");
    }

    @Override
    public String getSipServerHost() {
        return getPropertyAsString("sip-server-host");
    }

    @Override
    public Integer getSipServerPort() {
        return getPropertyAsInteger("sip-server-port");
    }

    @Override
    public String getSipProxyHost() {
        return getPropertyAsString("sip-proxy-host");
    }

    @Override
    public Integer getSipProxyPort() {
        return getPropertyAsInteger("sip-proxy-port");
    }

    @Override
    public Integer getSipExpires() {
        return getPropertyAsInteger("sip-expires");
    }

    @Override
    public String getSipUserAgent() {
        return getPropertyAsString("sip-user-agent");
    }

    @Override
    public String getMcChannel() {
        return getPropertyAsString("mc-channel");
    }

    @Override
    public String getApiAuthCheckUrl() {
        return getPropertyAsString("api-auth-check-url");
    }
}
