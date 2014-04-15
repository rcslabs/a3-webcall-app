package com.rcslabs.media;

import com.rcslabs.a3.IApplication;
import com.rcslabs.a3.rtc.IMediaContext;
import com.rcslabs.a3.rtc.IMediaPoint;
import com.rcslabs.a3.rtc.IMediaPointDelegate;
import com.rcslabs.calls.ClientCapabilities;
import com.rcslabs.a3.fsm.AbstractFSM;
import com.rcslabs.rcl.telephony.entity.ISdpObject;
import com.rcslabs.rcl.telephony.entity.SdpObject;
import com.rcslabs.webcall.MessageType;

import java.util.UUID;

public abstract class MediaPoint extends AbstractFSM<IMediaPoint.MediaPointState, MediaSignal> implements IMediaPoint{

    protected IApplication app;
    protected String pointId;
    protected String callId;
    protected String sessionId;
    protected ClientCapabilities cc;
    protected boolean voice;
    protected boolean video;
    protected String profile;
    protected ISdpObject sdp;
    protected IMediaPointDelegate enterStateDelegate;
    protected IMediaContext mediaContext;

    public IApplication getApp() {
        return app;
    }

    @Override
    public String getPointId() {
        return pointId;
    }

    @Override
    public String getCallId() {
        return callId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public ClientCapabilities getClientCapabilities() {
        return cc;
    }

    @Override
    public boolean hasVoice() {
        return voice;
    }

    @Override
    public boolean hasVideo() {
        return video;
    }

    @Override
    public String getProfile() {
        return profile;
    }

    @Override
    public ISdpObject getSDP() {
        return sdp;
    }

    @Override
    public IMediaContext getMediaContext() {
        return mediaContext;
    }

    @Override
    public void setMediaContext(IMediaContext mediaContext) {
        this.mediaContext = mediaContext;
    }

    public MediaPoint(String sessionId, String callId, ClientCapabilities cc,
                      boolean hasVoice, boolean hasVideo, String profile)
    {
        super();
        this.pointId = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        this.callId = callId;
        this.cc = cc;
        this.voice = hasVoice;
        this.video = hasVideo;
        this.profile = profile;
        this.sdp = new SdpObject();
        this.state = MediaPointState.INIT;
    }

    @Override
    public void initWith(IMediaPointDelegate enterStateDelegate) {
        this.enterStateDelegate = enterStateDelegate;
    }

    @Override
    public void onEvent(MediaSignal event) {
        switch(this.state)
        {
            case INIT:
                if(event.getType() == MessageType.SDP_OFFER) {
                    setState(MediaPointState.OFFERER_RECEIVED, event);
                } else if(event.getType() == MessageType.CRITICAL_ERROR) {
                    setState(MediaPointState.FAILED, event);
                }else {
                    unhandledEvent(event);
                }
                break;

            case OFFERER_RECEIVED:
                if(event.getType() == MessageType.SDP_ANSWER) {
                    setState(MediaPointState.ANSWERER_RECEIVED, event);
                } else if(event.getType() == MessageType.CRITICAL_ERROR) {
                    setState(MediaPointState.FAILED, event);
                }else {
                    unhandledEvent(event);
                }
                break;

            case ANSWERER_RECEIVED:
                if(event.getType() == MessageType.CREATE_MEDIA_POINT_OK) {
                    setState(MediaPointState.CREATED, event);
                } else if(event.getType() == MessageType.CREATE_MEDIA_POINT_FAILED) {
                    setState(MediaPointState.FAILED, event);
                } else if(event.getType() == MessageType.CRITICAL_ERROR) {
                    setState(MediaPointState.FAILED, event);
                }else {
                    unhandledEvent(event);
                }
                break;

            case CREATED:
                if(event.getType() == MessageType.JOIN_OK) {
                    setState(MediaPointState.JOINED, event);
                } else if(event.getType() == MessageType.JOIN_FAILED) {
                    setState(MediaPointState.FAILED, event);
                } else if(event.getType() == MessageType.CRITICAL_ERROR) {
                    setState(MediaPointState.FAILED, event);
                }else {
                    unhandledEvent(event);
                }
                break;

            case JOINED:
                if(event.getType() == MessageType.UNJOIN_OK) {
                    setState(MediaPointState.CREATED, event);
                } else if(event.getType() == MessageType.UNJOIN_FAILED) {
                    setState(MediaPointState.FAILED, event);
                } else if(event.getType() == MessageType.CRITICAL_ERROR) {
                    setState(MediaPointState.FAILED, event);
                }else {
                    unhandledEvent(event);
                }
                break;

            default:
                unhandledEvent(event);
                break;
        }
    }

    @Override
    protected void setState(MediaPointState state, MediaSignal event)
    {
        super.setState(state, event);

        switch (state) {
            case OFFERER_RECEIVED:  onSdpOffererReceived(event); break;
            case ANSWERER_RECEIVED: onSdpAnswererReceived(event); break;
            case CREATED:
                if(event.getType() == MessageType.CREATE_MEDIA_POINT_OK){
                    enterStateDelegate.onMediaPointCreated(this, event.getMessage());
                } else{ // unjoined from room
                    enterStateDelegate.onMediaPointUnjoinedFromRoom(this, event.getMessage());
                }
                break;
            case JOINED:  enterStateDelegate.onMediaPointJoinedToRoom(this, event.getMessage()); break;
            case FAILED: enterStateDelegate.onMediaFailed(this, event.getMessage()); break;
        }
    }

    abstract protected void onSdpOffererReceived(MediaSignal event);
    abstract protected void onSdpAnswererReceived(MediaSignal event);

}
