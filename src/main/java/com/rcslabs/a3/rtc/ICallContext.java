package com.rcslabs.a3.rtc;

import com.rcslabs.a3.fsm.IFSM;
import com.rcslabs.calls.CallSignal;

public interface ICallContext extends IFSM<ICallContext.CallState, CallSignal>{

    void initWith(ICallContextDelegate stateChangedDelegate);
    IMediaContext getMediaContext();
    void setMediaContext(IMediaContext value);
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
