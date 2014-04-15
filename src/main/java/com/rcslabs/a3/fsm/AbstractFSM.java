package com.rcslabs.a3.fsm;

import org.slf4j.Logger;

public abstract class AbstractFSM<S extends Enum, E extends IFSMSignal> implements IFSM<S, E>{

    protected S state;

    private Logger log;

    protected void setLogger(Logger log){
        this.log = log;
    }

    @Override
    public abstract void onEvent(E event);

    protected void setState(S state, E event){
        log.debug(this.state.name() + " -> " + state.name() + " " + this);
        this.state = state;
    }

    public S getState(){
        return state;
    }

    protected void unhandledEvent(E event){
        log.warn("Unhandled FSM event " + event + " " + this);  // WTF casting???
    }
}
