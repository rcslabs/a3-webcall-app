package com.rcslabs.fsm;

public interface IFSM<S extends Enum, E extends IFSMEvent> {

    S getState();

    void onEvent(E evt);
}
