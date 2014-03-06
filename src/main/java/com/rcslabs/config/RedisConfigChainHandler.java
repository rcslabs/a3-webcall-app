package com.rcslabs.config;

import redis.clients.jedis.Jedis;

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
        jedis.connect();
        if(jedis.exists("config:webcall-app:"+key)){
            return jedis.get("config:webcall-app:"+key);
        }
        if(null == next) return null;
        return next.getPropertyAsString(key);
    }
}
