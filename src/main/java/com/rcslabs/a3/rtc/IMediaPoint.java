package com.rcslabs.a3.rtc;

import com.rcslabs.calls.ClientCapabilities;
import com.rcslabs.a3.fsm.IFSM;
import com.rcslabs.media.MediaSignal;
import com.rcslabs.rcl.telephony.entity.ISdpObject;

public interface IMediaPoint extends IFSM<IMediaPoint.MediaPointState, MediaSignal>
{
    void initWith(IMediaPointDelegate stateChangedDelegate);
    IMediaContext getMediaContext();
    void setMediaContext(IMediaContext value);
    String getPointId();
    String getCallId();
    String getSessionId();
    ClientCapabilities getClientCapabilities();
    ISdpObject getSDP();
    boolean hasVoice();
    boolean hasVideo();
    String getProfile();

    public enum MediaPointState {
        INIT,  // wait SDP_OFFER by default
        OFFERER_RECEIVED,
        ANSWERER_RECEIVED,
        CREATED,
        FAILED,
        JOINED,
    }
}
