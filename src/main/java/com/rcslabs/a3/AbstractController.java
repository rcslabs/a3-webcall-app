package com.rcslabs.a3;

import com.rcslabs.a3.messaging.RedisConnector;

/**
 * Created by sx on 08.05.14.
 */
public abstract class AbstractController implements IController {

    protected final String channel;
    protected final RedisConnector redisConnector;

    public AbstractController(String channel, RedisConnector redisConnector){
        this.channel = channel;
        this.redisConnector = redisConnector;
    }

    @Override
    public String getChannel() {
        return channel;
    }
}
