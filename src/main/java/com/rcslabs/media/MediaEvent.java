package com.rcslabs.media;

import com.rcslabs.fsm.IFSMEvent;
import com.rcslabs.messaging.IMessage;
import com.rcslabs.webcall.MessageType;

public class MediaEvent implements IFSMEvent<MessageType> {

    private MessageType type;

    private IMessage originalMessage;

    public MessageType getType(){
        return type;
    }

    public IMessage getMessage(){
        return originalMessage;
    }

    public MediaEvent(IMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public MediaEvent(MessageType type){
        this.type = type;
    }
}
