package com.rcslabs.a3;

import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.messaging.RedisConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 12.05.14.
 */
public abstract class AbstractApplication implements IApplication {

    protected final Logger log;
    protected final String channelName;
    protected final RedisConnector redisConnector;
    protected final IConfig config;

    protected AbstractApplication(RedisConnector redisConnector, String channelName, IConfig config) {
        this.redisConnector = redisConnector;
        this.channelName = channelName;
        this.config = config;
        this.log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public String getChannel(){
        return channelName;
    }

    @Override
    public IConfig getConfig(){
        return config;
    }

    @Override
    public void start() {
        redisConnector.subscribe(this);
    }
}
