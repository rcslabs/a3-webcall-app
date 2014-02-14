package com.rcslabs.calls;

import com.rcslabs.fsm.IFSM;

public interface ICallContext extends IFSM<CallContext.CallState, CallEvent>{

    void initWith(ICallContextDelegate stateChangedDelegate);

    boolean hasVoice();

    boolean hasVideo();

    String getSipId();

    void setSipId(String sipId);

    String getSessionId();

    String getCallId();

    String getA();

    String getB();

    void set(String key, Object value);

    Object get(String key);

    boolean has(String key);

    void delete(String key);

    enum CallState {
        INIT,
        STARTING,
        STARTING_INCOMING,
        STARTED,
        FAILED,
        FINISHED
    }
}
