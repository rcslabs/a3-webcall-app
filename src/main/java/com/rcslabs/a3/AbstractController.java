package com.rcslabs.a3;

import com.rcslabs.a3.messaging.RedisConnector;

/**
 * Created by sx on 08.05.14.
 */
public abstract class AbstractController implements IController {

    protected final String channelName;
    protected final RedisConnector redisConnector;

    public AbstractController(RedisConnector redisConnector, String channelName){
        this.channelName = channelName;
        this.redisConnector = redisConnector;
    }

    @Override
    public String getChannel() {
        return channelName;
    }
}
