package com.rcslabs.a3;

import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.exception.ComponentLifecycleException;
import com.rcslabs.redis.RedisConnector;

/**
 * Created by sx on 12.05.14.
 */
public abstract class AbstractApplication extends AbstractComponent implements IApplication {

    protected final IConfig config;
    protected final RedisConnector redisConnector;

    protected AbstractApplication(String name, RedisConnector redisConnector, IConfig config) {
        super(name);
        this.config = config;
        this.redisConnector = redisConnector;
    }

    @Override
    public void init() throws ComponentLifecycleException
    {
        super.init();
        redisConnector.addMessageListener(name, this);
    }

    @Override
    public IConfig getConfig(){
        return config;
    }

}
