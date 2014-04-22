package com.rcslabs.webcall.calls;

import com.rcslabs.a3.messaging.AbstractMessage;

/**
 * Created by sx on 15.04.14.
 */
public class CallMessage extends AbstractMessage<CallMessage.Type> {

    public static enum Type {
        START_CALL,
        INCOMING_CALL,
        REJECT_CALL,
        ACCEPT_CALL,
        HANGUP_CALL,
        CALL_STARTING,
        CALL_STARTED,
        CALL_FAILED,
        CALL_FINISHED,
        CALL_FINISH_NOTIFICATION, // TODO: move this out
        SEND_DTMF
    }

    public CallMessage(Type type){
        super(type);
    }
}
