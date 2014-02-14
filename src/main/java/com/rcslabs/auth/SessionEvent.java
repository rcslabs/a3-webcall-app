package com.rcslabs.auth;

import com.rcslabs.fsm.IFSMEvent;
import com.rcslabs.webcall.MessageType;

public class SessionEvent implements IFSMEvent<MessageType> {

    private MessageType type;

    public MessageType getType(){
        return type;
    }

    public SessionEvent(MessageType type){
        this.type = type;
    }
}
