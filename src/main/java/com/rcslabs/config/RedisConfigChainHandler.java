package com.rcslabs.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by sx on 06.03.14.
 */
public class RedisConfigChainHandler extends AbstractConfigChainHandler {

    private Jedis jedis;

    public RedisConfigChainHandler(String host, int port){
        super();
        jedis = new Jedis(host, port);
    }

    @Override
    public String getPropertyAsString(String key){
        try{
            if(jedis.exists(key)){
                return jedis.get(key);
            }
        } catch (JedisConnectionException e){ /* nothing happens */ }

        if(null == next) return null;
        return next.getPropertyAsString(key);
    }

}
