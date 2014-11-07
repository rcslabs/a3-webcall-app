package com.rcslabs.a3.rtc;

import com.rcslabs.a3.messaging.IAlenaMessage;

public interface ICallContextDelegate {
    void onCallStarting(ICallContext ctx, IAlenaMessage message);
    void onIncomingCall(ICallContext ctx, IAlenaMessage message);
    void onCallStarted(ICallContext ctx, IAlenaMessage message);
    void onCallFinished(ICallContext ctx, IAlenaMessage message);
    void onCallFailed(ICallContext ctx, IAlenaMessage message);
}
