package com.rcslabs.webcall.calls;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rcslabs.a3.messaging.RedisConnector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CallLogger {

    private static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    private RedisConnector redisConnector;

    public CallLogger(RedisConnector redisConnector){
        this.redisConnector = redisConnector;
    }

    public void push(CallLogEntry item){
        Jedis jedis = null;
        try{
            jedis = redisConnector.getResource();
            jedis.ping();
            String json = gson.toJson(item);
            jedis.publish("log:calls", json);
            jedis.rpush("log:calls", json);
            redisConnector.returnResource(jedis);
        } catch (Exception e){
            redisConnector.returnBrokenResource(jedis);
        }
    }
}
