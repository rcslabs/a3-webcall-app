package com.rcslabs.a3.test;

import com.rcslabs.a3.auth.AuthMessage;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.MessageMarshaller;
import com.rcslabs.a3.messaging.RedisConnector;
import com.rcslabs.chat.ChatMessage;
import com.rcslabs.webcall.calls.CallMessage;
import com.rcslabs.webcall.media.MediaMessage;
import org.junit.*;

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

    private static RedisConnector redisConnector;

    private static TestMessageBrokerDelegate subscriber;

    private static final String REDIS_URI = "redis://192.168.1.200";
    private static final String CHANNEL = "test";

    @BeforeClass
    public static void setUpClass() throws Exception {
        redisConnector = new RedisConnector();
        redisConnector.connect(new URI(REDIS_URI));
        subscriber = new TestMessageBrokerDelegate(CHANNEL);
        redisConnector.subscribe(subscriber);

        MessageMarshaller m = MessageMarshaller.getInstance();
        m.registerMessageClass(AuthMessage.class);
        m.registerMessageClass(CallMessage.class);
        m.registerMessageClass(MediaMessage.class);
        m.registerMessageClass(ChatMessage.class);
        m.start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        redisConnector.unubscribe(subscriber);
    }

    @Before
    public void setUp(){
        subscriber.clean();
    }

    @Test
    public void testCloneMessage(){
        IMessage m1 = new AuthMessage(AuthMessage.Type.START_SESSION);
        m1.set("username", "A");
        m1.set("password", "B");

        IMessage m2 = m1.cloneWithSameType();
        Assert.assertNotSame(m1, m2);
        Assert.assertEquals(m1.getTypz(), m2.getTypz());
        Assert.assertEquals(m1.getType(), m2.getType());
        Assert.assertEquals(m1.get("username"), m2.get("username"));
        Assert.assertEquals(m1.get("password"), m2.get("password"));

        IMessage m3 = m2.cloneWithAnyType(CallMessage.Type.START_CALL);
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
            Thread.sleep(1000);
            List<IMessage> resultList = subscriber.getMessages();
            Assert.assertEquals(resultList.size(), types.size());
            for(IMessage m : resultList){
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
                IMessage aMessage = (IMessage)cnst.newInstance(t);
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
