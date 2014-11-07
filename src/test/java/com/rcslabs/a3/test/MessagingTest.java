package com.rcslabs.a3.test;

import com.rcslabs.a3.messaging.AuthMessage;
import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.a3.messaging.JsonMessageSerializer;
import com.rcslabs.chat.ChatMessage;
import com.rcslabs.a3.messaging.CallMessage;
import com.rcslabs.a3.messaging.MediaMessage;
import com.ykrkn.redis.RedisConnector;
import org.junit.*;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sx on 07.05.14.
 */
@Ignore
public class MessagingTest {

    private JedisPool pool;
    private RedisConnector redisConnector;
    private TestMessageBrokerDelegate subscriber;
    private static final String REDIS_URI = "redis://192.168.1.200";
    private static final String CHANNEL = "test";

    @Before
    public void setUp() throws Exception {
        URI uri = new URI(REDIS_URI);
        pool = new JedisPool(uri.getHost(), (-1 == uri.getPort() ? Protocol.DEFAULT_PORT : uri.getPort()));
        redisConnector = new RedisConnector(pool);
        JsonMessageSerializer s = new JsonMessageSerializer();
        s.registerMessageClass(AuthMessage.class);
        s.registerMessageClass(CallMessage.class);
        s.registerMessageClass(MediaMessage.class);
        s.registerMessageClass(ChatMessage.class);
        redisConnector.addSerializer(s);
        redisConnector.subscribe();

        subscriber = new TestMessageBrokerDelegate(CHANNEL);
        redisConnector.addMessageListener(CHANNEL, subscriber);
        Thread.sleep(1111);
    }

    @After
    public void tearDown() throws Exception {
        redisConnector.dispose();
        redisConnector = null;
        pool.destroy();
        pool = null;
    }

    @Test
    public void testCloneMessage(){
        IAlenaMessage m1 = new AuthMessage(AuthMessage.Type.START_SESSION);
        m1.set("username", "A");
        m1.set("password", "B");

        IAlenaMessage m2 = m1.cloneWithSameType();
        Assert.assertNotSame(m1, m2);
        Assert.assertEquals(m1.getTypz(), m2.getTypz());
        Assert.assertEquals(m1.getType(), m2.getType());
        Assert.assertEquals(m1.get("username"), m2.get("username"));
        Assert.assertEquals(m1.get("password"), m2.get("password"));

        IAlenaMessage m3 = m2.cloneWithAnyType(CallMessage.Type.START_CALL);
        Assert.assertEquals(m3.getTypz(), "CallMessage");
        Assert.assertEquals(m3.getType(), CallMessage.Type.START_CALL);
        Assert.assertEquals(m1.get("username"), m3.get("username"));
        Assert.assertEquals(m1.get("password"), m3.get("password"));
    }

    @Test
    public void testAuthMessage(){
        testMessage(AuthMessage.class, "AuthMessage");
    }

    @Test
    public void testCallMessage(){
        testMessage(CallMessage.class, "CallMessage");
    }

    @Test
    public void testMediaMessage(){
        testMessage(MediaMessage.class, "MediaMessage");
    }

    @Test
    public void testChatMessage(){
        testMessage(ChatMessage.class, "ChatMessage");
    }


    private void testMessage(Class<?> clazz, String typeOfClass)
    {
        try{
            List types = getMessageClassTypesAsEnum(clazz);
            Assert.assertTrue(0 != types.size());
            testPublishAllTypesOfMessage(clazz);
            Thread.sleep(1111);
            List<IAlenaMessage> resultList = subscriber.getMessages();
            Assert.assertEquals(types.size(), resultList.size());
            for(IAlenaMessage m : resultList){
                Assert.assertEquals(m.getTypz(), typeOfClass);
                Assert.assertEquals(m.getClass(), clazz);
            }
        } catch ( InterruptedException e){
            Assert.fail();
        }
    }

    private void testPublishAllTypesOfMessage(Class<?> clazz)
    {
        try{
            List<?> classTypesEnum = getMessageClassTypesAsEnum(clazz);
            for(Object t : classTypesEnum){
                Constructor<?> cnst = clazz.getConstructor(t.getClass());
                IAlenaMessage aMessage = (IAlenaMessage)cnst.newInstance(t);
                redisConnector.publish(CHANNEL, aMessage);
            }
        } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
            Assert.fail();
        }
    }

    private List<String> getMessageClassTypesAsString(Class<?> clazz)
    {
        List<String> result = new ArrayList<>();
        Class[] classes = clazz.getDeclaredClasses();
        for(Class c : classes){
            if(c.getName().endsWith("Type")){
                Object[] enumCnst = c.getEnumConstants();
                for(Object o : enumCnst){
                    result.add(o.toString());
                }
            }
        }
        return result;
    }

    public <T> List<T> getMessageClassTypesAsEnum(Class<?> clazz)
    {
        List<T> result = new ArrayList<>();
        Class[] classes = clazz.getDeclaredClasses();
        for(Class c : classes){
            if(c.getName().endsWith("Type")){
                Object[] enumCnst = c.getEnumConstants();
                for(Object o : enumCnst){
                    result.add((T)o);
                }
            }
        }
        return result;
    }
}
