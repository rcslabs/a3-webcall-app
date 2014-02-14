package com.rcslabs.calls;

import com.rcslabs.messaging.IMessage;
import com.rcslabs.messaging.Message;
import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.event.*;
import com.rcslabs.webcall.ICallApplication;
import com.rcslabs.webcall.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  ICallListener implementation
 *  Simply convert rcl-api events into "messages", rethrow ones for
 *  handle in onMessageReceived
 */
public class JainSipCallListener implements ICallListener {

    protected final static Logger log = LoggerFactory.getLogger(JainSipCallListener.class);

    private ICallApplication app;

    public JainSipCallListener(ICallApplication app){
        this.app = app;
    }

    @Override
    public void onCallStarting(ICallStartingEvent event) {
        log.info(event.toString());
        IMessage message = new Message(MessageType.CALL_STARTING);
        ICallContext ctx = app.findCallContext(event.getCall().getId());
        if(null != ctx){
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            message.set(IMessage.PROP_STAGE, event.getStage().toString());
            message.set(IMessage.PROP_SDP, ((ICallParams)event.getCall()).getSdpObject().getOfferer());
            ctx.onEvent(new CallEvent(message));
        } else {
            log.warn("Call " + event.getCall().getId() + " not found");
        }
    }

    @Override
    public void onCallStarted(ICallEvent event) {
        log.info(event.toString());
        IMessage message = new Message(MessageType.CALL_STARTED);
        ICallContext ctx = app.findCallContext(event.getCall().getId());
        if(null != ctx){
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            message.set(IMessage.PROP_SDP, ((ICallParams)event.getCall()).getSdpObject().getAnswerer());
            ctx.onEvent(new CallEvent(message));
        } else {
            log.warn("Call " + event.getCall().getId() + " not found");
        }
    }

    @Override
    public void onCallFinished(ICallEvent event) {
        log.info(event.toString());
        IMessage message = new Message(MessageType.CALL_FINISHED);
        ICallContext ctx = app.findCallContext(event.getCall().getId());
        if(null != ctx){
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            ctx.onEvent(new CallEvent(message));
        } else {
            log.warn("Call " + event.getCall().getId() + " not found");
        }
        event.getCall().removeListener(this);
    }

    @Override
    public void onCallFailed(ICallFailedEvent event) {
        log.info(event.toString());
        IMessage message = new Message(MessageType.CALL_FAILED);
        ICallContext ctx = app.findCallContext(event.getCall().getId());
        if(null != ctx){
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            message.set(IMessage.PROP_REASON, event.getRejectReason().toString());
            ctx.onEvent(new CallEvent(message));
        } else {
            log.warn("Call " + event.getCall().getId() + " not found");
        }
        event.getCall().removeListener(this);
    }

    @Override
    public void onCallTransfered(ICallTransferEvent event) {
        log.info(event.toString());
    }

    @Override
    public void onTransferFailed(ICallTransferEvent event) {
        log.info(event.toString());
    }

    @Override
    public void onCallError(ICallEvent event) {
        log.info(event.toString());
        IMessage message = new Message(MessageType.CALL_FAILED);
        ICallContext ctx = app.findCallContext(event.getCall().getId());
        if(null != ctx){
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            ErrorInfo e = event.getErrorInfo();
            if(null == e){
                message.set(IMessage.PROP_REASON, "ERROR");
            }else{
                message.set(IMessage.PROP_REASON, e.getErrorCode() + " : " + e.getErrorText());
            }

            ctx.onEvent(new CallEvent(message));
        } else {
            log.warn("Call" + event.getCall().getId() + " not found");
        }
        event.getCall().removeListener(this);
    }

    @Override
    public void onCallFinishNotification(ICallFinishNotificationEvent event) {
        log.info(event.toString());
    }
}
