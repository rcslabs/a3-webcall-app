package com.rcslabs.auth;

import com.rcslabs.messaging.Message;

/**
 * Created by sx on 15.04.14.
 */
public class AuthMessage extends Message<AuthMessage.Type> {

    public static enum Type {
        START_SESSION,
        SESSION_STARTED,
        SESSION_FAILED,
        CLOSE_SESSION,
        SESSION_CLOSED
    }

    public AuthMessage(Type type){
        super(type);
    }
}
