package com.rcslabs.webcall.calls;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.a3.messaging.CallMessage;
import com.rcslabs.a3.messaging.IAlenaMessage;

public class CallSignal implements IFSMSignal<CallMessage.Type> {

    private CallMessage.Type type;

    private IAlenaMessage originalMessage;

    public CallMessage.Type getType(){
        return type;
    }

    public IAlenaMessage getMessage(){
        return originalMessage;
    }

    public CallSignal(CallMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public CallSignal(CallMessage.Type type){
        this.type = type;
    }
}
