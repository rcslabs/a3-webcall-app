package com.rcslabs.calls;

import com.rcslabs.webcall.MessageType;

import java.io.Serializable;
import java.util.Date;

public class CallLogEntry implements Serializable {

    public static final long serialVersionUID = 1L;

    private long timestamp;
    private String callId;
    private String sipId;
    private String type;
    private String a;
    private String b;
    private String details;

    public CallLogEntry(MessageType type, ICallContext ctx, String details){
        this(type, ctx);
        this.details = details;
    }

    public CallLogEntry(MessageType messageType, ICallContext ctx){
        timestamp = (new Date()).getTime();
        type = messageType.name();
        callId = ctx.getCallId();
        sipId = ctx.getSipId();
        a = ctx.getA();
        b = ctx.getB();
    }
}
