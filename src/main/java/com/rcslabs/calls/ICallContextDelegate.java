package com.rcslabs.calls;

import com.rcslabs.messaging.IMessage;

public interface ICallContextDelegate {
    void onCallStarting(ICallContext ctx, IMessage message);
    void onIncomingCall(ICallContext ctx, IMessage message);
    void onCallStarted(ICallContext ctx, IMessage message);
    void onCallFinished(ICallContext ctx, IMessage message);
    void onCallFailed(ICallContext ctx, IMessage message);
}
