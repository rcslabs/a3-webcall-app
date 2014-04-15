package com.rcslabs.media;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.messaging.IMessage;
import com.rcslabs.webcall.MessageType;

public class MediaSignal implements IFSMSignal<MessageType> {

    private MessageType type;

    private IMessage originalMessage;

    public MessageType getType(){
        return type;
    }

    public IMessage getMessage(){
        return originalMessage;
    }

    public MediaSignal(IMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public MediaSignal(MessageType type){
        this.type = type;
    }
}
