package com.rcslabs.calls;

import com.rcslabs.messaging.IMessage;
import com.rcslabs.fsm.IFSMEvent;
import com.rcslabs.webcall.MessageType;

public class CallEvent implements IFSMEvent<MessageType> {

    private MessageType type;

    private IMessage originalMessage;

    public MessageType getType(){
        return type;
    }

    public IMessage getMessage(){
        return originalMessage;
    }

    public CallEvent(IMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public CallEvent(MessageType type){
        this.type = type;
    }
}
