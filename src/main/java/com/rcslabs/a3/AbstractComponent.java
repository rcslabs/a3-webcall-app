package com.rcslabs.a3;

import com.rcslabs.a3.exception.ComponentLifecycleException;
import com.rcslabs.a3.messaging.RedisConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by sx on 15.05.14.
 */
public abstract class AbstractComponent implements IComponent{

    protected final Logger log;
    protected final String channelName;
    protected final RedisConnector redisConnector;
    protected final String componentKey;
    protected final String aliveKey;

    private ScheduledExecutorService scheduledExecutorService;

    public AbstractComponent(RedisConnector redisConnector, String channelName){
        this.channelName = channelName;
        this.redisConnector = redisConnector;
        this.log = LoggerFactory.getLogger(getClass());
        this.componentKey = channelName + ":" + ManagementFactory.getRuntimeMXBean().getName();
        this.aliveKey =   componentKey + ":alive";
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void init() throws ComponentLifecycleException
    {
        log.info("Init " + componentKey);

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                    try{
                    Jedis j = redisConnector.getResource();
                    if(null == j) return;
                    j.incr(aliveKey);
                    j.pexpire(aliveKey, 3000);
                    redisConnector.returnResource(j);
                }catch(Exception e){
                    log.error(e.getMessage());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        redisConnector.subscribe(this);
    }

    @Override
    public void destroy() throws ComponentLifecycleException {
        log.info("Destroy " + componentKey);
    }

    @Override
    public String getChannel() {
        return channelName;
    }
}
