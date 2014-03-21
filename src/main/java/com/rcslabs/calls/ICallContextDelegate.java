package com.rcslabs.calls;

import com.rcslabs.webcall.AlenaMessage;

public interface ICallContextDelegate {
    void onCallStarting(ICallContext ctx, AlenaMessage message);
    void onIncomingCall(ICallContext ctx, AlenaMessage message);
    void onCallStarted(ICallContext ctx, AlenaMessage message);
    void onCallFinished(ICallContext ctx, AlenaMessage message);
    void onCallFailed(ICallContext ctx, AlenaMessage message);
}
