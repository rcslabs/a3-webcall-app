package com.rcslabs.a3;

import com.rcslabs.a3.messaging.RedisConnector;

/**
 * Created by sx on 08.05.14.
 */
public abstract class AbstractController extends AbstractComponent implements IController {

    public AbstractController(RedisConnector redisConnector, String channelName) {
        super(redisConnector, channelName);
    }
}
