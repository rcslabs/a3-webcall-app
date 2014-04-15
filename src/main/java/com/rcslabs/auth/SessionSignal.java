package com.rcslabs.auth;

import com.rcslabs.a3.fsm.IFSMSignal;
import com.rcslabs.webcall.MessageType;

public class SessionSignal implements IFSMSignal<MessageType> {

    private MessageType type;

    public MessageType getType(){
        return type;
    }

    public SessionSignal(MessageType type){
        this.type = type;
    }
}
