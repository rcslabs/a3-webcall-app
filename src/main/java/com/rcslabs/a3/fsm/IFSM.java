package com.rcslabs.a3.fsm;

public interface IFSM<S extends Enum, E extends IFSMSignal> {

    S getState();

    void onEvent(E evt);
}
