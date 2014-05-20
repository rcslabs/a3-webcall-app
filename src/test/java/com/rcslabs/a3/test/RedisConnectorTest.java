package com.rcslabs.a3.test;

import com.rcslabs.a3.messaging.MessageMarshaller;
import com.rcslabs.a3.messaging.RedisConnector;
import org.junit.*;
import redis.clients.jedis.Jedis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Ignore("This test only for manual run")
public class RedisConnectorTest {

    private RedisConnector connector;

    private URI getValidURI(){
        try {
            return new URI("redis://192.168.1.200");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private URI getInvalidURI(){
        try {
            return new URI("redis://10.10.10.10");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        MessageMarshaller m = MessageMarshaller.getInstance();
        m.registerMessageClass(TestMessage.class);
        m.start();
    }

    @Before
    public void setUp(){
        connector = new RedisConnector();
    }

    @After
    public void tearDown(){
        connector.dispose();
        connector = null;
    }

    @Test
    public void testConnectForInvalidURI() throws InterruptedException {
        final URI uri = getInvalidURI();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                RedisConnector connector = new RedisConnector();
                connector.connect(uri);
            }
        });

        t.start();
        Thread.sleep(7777);
        // if no exceptions or errors this is ok
        t.interrupt();
    }

    @Test
    public void testConnectForValidURI() throws InterruptedException {
        connector.connect(getValidURI());
        Jedis j = connector.getResource();
        Assert.assertTrue(j != null);
        Assert.assertTrue(j.isConnected());
    }

    @Test
    public void testSubscribe() throws InterruptedException {
        connector.connect(getValidURI());
        TestMessageBrokerDelegate dA = new TestMessageBrokerDelegate("A");
        connector.subscribe(dA);
        TestMessageBrokerDelegate dB = new TestMessageBrokerDelegate("B");
        connector.subscribe(dB);
        int i = 1000;
        while(i-- > 0){
            connector.publish("A", new TestMessage(TestMessage.Type.TEST));
            connector.publish("B", new TestMessage(TestMessage.Type.TEST));
        }
        Thread.sleep(3333); // all messages received
        Assert.assertEquals(1000, dA.getMessages().size());
        Assert.assertEquals(1000, dB.getMessages().size());
    }

    @Test
    public void testReconnect()  throws InterruptedException{
        connector.connect(getValidURI());
        TestMessageBrokerDelegate dA = new TestMessageBrokerDelegate("A");
        connector.subscribe(dA);
        connector.publish("A", new TestMessage(TestMessage.Type.TEST));
        Thread.sleep(999); // all messages received
        Assert.assertEquals(1, dA.getMessages().size());

        System.out.println("Hey man! You have 12 seconds to break and fix a connection.");
        System.out.println("Ready, steady, go!");
        Thread.sleep(12000);

        Jedis j = connector.getResource();
        Assert.assertTrue(j.isConnected());
        j.ping(); // expects no exceptions in this line

        dA.clean();
        connector.publish("A", new TestMessage(TestMessage.Type.TEST));
        Thread.sleep(999); // all messages received
        Assert.assertEquals(1, dA.getMessages().size());
    }

    @Test
    public void testUnsubscribeAndSubscribeAgain()  throws InterruptedException{
        connector.connect(getValidURI());
        TestMessageBrokerDelegate dA = new TestMessageBrokerDelegate("A");
        connector.subscribe(dA);
        connector.publish("A", new TestMessage(TestMessage.Type.TEST));
        Thread.sleep(999); // all messages received
        Assert.assertEquals(1, dA.getMessages().size());

        connector.unubscribe(dA);

        dA.clean();
        connector.publish("A", new TestMessage(TestMessage.Type.TEST));
        Thread.sleep(999); // all messages (NOT) received
        Assert.assertEquals(0, dA.getMessages().size());

        dA.clean();
        connector.subscribe(dA);
        Thread.sleep(999);
        connector.publish("A", new TestMessage(TestMessage.Type.TEST));
        Thread.sleep(999); // all messages received
        Assert.assertEquals(1, dA.getMessages().size());
    }

    @Test(timeout = 30000)
    public void testGrowingJedisPool()  throws InterruptedException{
        connector.connect(getValidURI());

        List<TestMessageBrokerDelegate> delegates = new ArrayList<>();

        for(int i=0; i<60; ++i){
            System.out.println("Subscribers count: " + i);
            TestMessageBrokerDelegate dA = new TestMessageBrokerDelegate("A");
            connector.subscribe(dA);
            delegates.add(dA);
        }

        Thread.sleep(999);

        int i = 1000;
        while(i-- > 0){
            connector.publish("A", new TestMessage(TestMessage.Type.TEST));
        }
        Thread.sleep(999);  // all messages received?

        for(TestMessageBrokerDelegate d : delegates){
            Assert.assertEquals(1000, d.getMessages().size());
        }
    }

    @Test
    public void testSimpleKey(){
        connector.connect(getValidURI());
        int i = 1000;
        while(i-- > 0){
            connector.getResource().set("test", ""+i);
        }
    }

    @Test
    public void testMultithreadKey() throws InterruptedException{
        connector.connect(getValidURI());
        ExecutorService service = Executors.newFixedThreadPool(16);
        int i = 16;
        while(i-- > 0){
            service.execute(new Runnable() {
                @Override
                public void run() {
                    int j = 1000;
                    while(j-- > 0){
                        connector.getResource().set("test", ""+j);
                    }
                }
            });
        }

        Thread.sleep(9999);
    }
}
