package com.rcslabs.calls;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.messaging.IMessage;
import com.rcslabs.webcall.MessageType;

public class CallSignal implements IFSMSignal<MessageType> {

    private MessageType type;

    private IMessage originalMessage;

    public MessageType getType(){
        return type;
    }

    public IMessage getMessage(){
        return originalMessage;
    }

    public CallSignal(IMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public CallSignal(MessageType type){
        this.type = type;
    }
}
