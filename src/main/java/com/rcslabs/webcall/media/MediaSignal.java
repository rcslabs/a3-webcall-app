package com.rcslabs.webcall.media;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.a3.messaging.MediaMessage;

public class MediaSignal implements IFSMSignal<MediaMessage.Type> {

    private MediaMessage.Type type;

    private IAlenaMessage originalMessage;

    public MediaMessage.Type getType(){
        return type;
    }

    public IAlenaMessage getMessage(){
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
