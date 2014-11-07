package com.rcslabs.webcall.calls;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rcslabs.redis.RedisConnector;
import redis.clients.jedis.Jedis;

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
        Jedis j = redisConnector.getResource();
        if(null == j){ return;}
        String json = gson.toJson(item);
        j.publish("log:calls", json);
        j.rpush("log:calls", json);
        redisConnector.returnResource(j);
    }
}
