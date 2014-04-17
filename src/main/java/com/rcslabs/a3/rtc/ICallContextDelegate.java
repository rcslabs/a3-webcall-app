package com.rcslabs.a3.rtc;

import com.rcslabs.a3.messaging.IMessage;

public interface ICallContextDelegate {
    void onCallStarting(ICallContext ctx, IMessage message);
    void onIncomingCall(ICallContext ctx, IMessage message);
    void onCallStarted(ICallContext ctx, IMessage message);
    void onCallFinished(ICallContext ctx, IMessage message);
    void onCallFailed(ICallContext ctx, IMessage message);
}
