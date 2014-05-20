package com.rcslabs.a3.test;

import com.rcslabs.a3.messaging.AbstractMessage;

/**
 * Created by sx on 16.05.14.
 */
public class TestMessage extends AbstractMessage<TestMessage.Type> {

    public static enum Type {
        TEST
    }

    public TestMessage(Type type){
        super(type);
    }

}
