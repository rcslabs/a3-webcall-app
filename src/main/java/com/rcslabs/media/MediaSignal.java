package com.rcslabs.media;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.messaging.IMessage;

public class MediaSignal implements IFSMSignal<MediaMessage.Type> {

    private MediaMessage.Type type;

    private IMessage originalMessage;

    public MediaMessage.Type getType(){
        return type;
    }

    public IMessage getMessage(){
        return originalMessage;
    }

    public MediaSignal(MediaMessage msg){
        this(msg.getType());
        this.originalMessage = msg;
    }

    public MediaSignal(MediaMessage.Type type){
        this.type = type;
    }
}
