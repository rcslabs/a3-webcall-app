package com.rcslabs.webcall.calls;

import com.rcslabs.a3.messaging.MessageProperty;
import com.rcslabs.a3.rtc.ICallContext;

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
    private String buttonId; // TODO: move it out

    public CallLogEntry(CallMessage.Type type, ICallContext ctx, String details){
        this(type, ctx);
        this.details = details;
    }

    public CallLogEntry(CallMessage.Type messageType, ICallContext ctx){
        timestamp = (new Date()).getTime();
        type = messageType.name();
        callId = ctx.getCallId();
        sipId = ctx.getSipId();
        a = ctx.getA();
        b = ctx.getB();

        // TODO: move it out
        if(ctx.has(MessageProperty.PROJECT_ID)){
            this.buttonId = ""+ctx.get(MessageProperty.PROJECT_ID);
        }
    }
}
