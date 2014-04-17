package com.rcslabs.calls;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.a3.messaging.IMessage;

public class CallSignal implements IFSMSignal<CallMessage.Type> {

    private CallMessage.Type type;

    private IMessage originalMessage;

    public CallMessage.Type getType(){
        return type;
    }

    public IMessage getMessage(){
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
