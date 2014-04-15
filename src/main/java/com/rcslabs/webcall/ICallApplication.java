package com.rcslabs.webcall;

import com.rcslabs.a3.IApplication;
import com.rcslabs.a3.rtc.ICallContext;
import com.rcslabs.a3.rtc.IMediaPoint;
import com.rcslabs.calls.ClientCapabilities;
import com.rcslabs.messaging.IMessage;
import com.rcslabs.messaging.IMessageBrokerDelegate;

import java.util.List;

public interface ICallApplication extends IApplication, IMessageBrokerDelegate{

    ICallContext createCallContext(IMessage message);

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
