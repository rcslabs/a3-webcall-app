package com.rcslabs.webcall.calls;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CallLogger {

    private static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    private JedisPool jedisPool;

    public CallLogger(JedisPool jedisPool){
        this.jedisPool = jedisPool;
    }

    public void push(CallLogEntry item){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.ping();
            String json = gson.toJson(item);
            jedis.publish("log:calls", json);
            jedis.rpush("log:calls", json);
            jedisPool.returnResource(jedis);
        } catch (Exception e){
            jedisPool.returnBrokenResource(jedis);
        }
    }
}
