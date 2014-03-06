package com.rcslabs.config;

import com.rcslabs.webcall.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by sx on 06.03.14.
 */
public abstract class AbstractConfigChainHandler implements IConfig, IConfigChainHandler {

    protected final static Logger log = LoggerFactory.getLogger(AbstractConfigChainHandler.class);

    protected Properties data;
    protected IConfigChainHandler next;

    protected AbstractConfigChainHandler(){
        data = new Properties();
    }

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
    public String getMessagingHost() {
        String mb = getPropertyAsString("messaging-uri");
        if(null == mb){ return null; }
        try {
            URI u = new URI(mb);
            return u.getHost();
        } catch (URISyntaxException e) {
            log.error("Error parse messaging URI: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Integer getMessagingPort() {
        String mb = getPropertyAsString("messaging-uri");
        if(null == mb){ return null; }
        try {
            URI u = new URI(mb);
            return (-1 == u.getPort() ? 6379 : u.getPort());
        } catch (URISyntaxException e) {
            log.error("Error parse messaging URI: " + e.getMessage());
        }
        return null;
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
