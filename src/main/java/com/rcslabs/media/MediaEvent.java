package com.rcslabs.media;

import com.rcslabs.fsm.IFSMEvent;
import com.rcslabs.webcall.AlenaMessage;
import com.rcslabs.webcall.MessageType;

public class MediaEvent implements IFSMEvent<MessageType> {

    private MessageType type;

    private AlenaMessage originalMessage;

    public MessageType getType(){
        return type;
    }

    public AlenaMessage getMessage(){
        return originalMessage;
    }

    public MediaEvent(AlenaMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public MediaEvent(MessageType type){
        this.type = type;
    }
}
