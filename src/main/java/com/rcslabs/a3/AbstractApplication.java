package com.rcslabs.a3;

import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.messaging.RedisConnector;

/**
 * Created by sx on 12.05.14.
 */
public abstract class AbstractApplication extends AbstractComponent implements IApplication {

    protected final IConfig config;

    protected AbstractApplication(RedisConnector redisConnector, String channelName, IConfig config) {
        super(redisConnector, channelName);
        this.config = config;
    }

    @Override
    public IConfig getConfig(){
        return config;
    }

}
