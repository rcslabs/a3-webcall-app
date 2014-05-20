package com.rcslabs.a3.config;

import com.rcslabs.a3.messaging.RedisConnector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by sx on 06.03.14.
 */
public class RedisConfigChainHandler extends AbstractConfigChainHandler {

    private RedisConnector redisConnector;

    public RedisConfigChainHandler(RedisConnector redisConnector){
        super();
        this.redisConnector = redisConnector;
    }

    @Override
    public String getPropertyAsString(String key){
        Jedis j = redisConnector.getResource();
        if(null != j && j.exists(key)){
            String result = j.get(key);
            redisConnector.returnResource(j);
            return result;
        }else{
            if(null != next){
                return next.getPropertyAsString(key);
            }else{
                return null;
            }
        }
    }
}
