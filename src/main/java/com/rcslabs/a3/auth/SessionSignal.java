package com.rcslabs.a3.auth;

import com.rcslabs.a3.fsm.IFSMSignal;

public class SessionSignal implements IFSMSignal<AuthMessage.Type> {

    private AuthMessage.Type type;

    public AuthMessage.Type getType(){
        return type;
    }

    public SessionSignal(AuthMessage.Type type){
        this.type = type;
    }
}
