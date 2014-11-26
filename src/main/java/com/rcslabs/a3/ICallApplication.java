package com.rcslabs.a3;

import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.a3.rtc.ICallContext;
import com.rcslabs.a3.rtc.IMediaPoint;
import com.rcslabs.webcall.calls.ClientCapabilities;

import java.util.List;

public interface ICallApplication extends IApplication {

    ICallContext createCallContext(IAlenaMessage message);

    ICallContext findCallContext(String callId);

    void startCall(ICallContext ctx, String sdpOfferer);

    IMediaPoint findMediaPoint(String pointId);

    List<IMediaPoint> findMediaPointsByCallId(String callId);

    IMediaPoint createWRTCMediaPoint(String sessionId, String callId, ClientCapabilities cc, boolean hasVoice, boolean hasVideo);

    IMediaPoint createSIPMediaPoint(String sessionId, String callId, boolean hasVoice, boolean hasVideo);

    void removeMediaPoint(String pointId);

    void joinRoom(String pointId, String roomId);

    void unjoinRoom(String pointId, String roomId);

    void stopMedia(String callId);

    void sendDTMF(String callId, String dtmf);
}
