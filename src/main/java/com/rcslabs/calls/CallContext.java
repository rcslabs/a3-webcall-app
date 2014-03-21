package com.rcslabs.calls;

import com.rcslabs.fsm.AbstractFSM;
import com.rcslabs.media.IMediaContext;
import com.rcslabs.webcall.AlenaMessage;
import com.rcslabs.webcall.ICallApplication;
import com.rcslabs.webcall.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CallContext extends AbstractFSM<ICallContext.CallState, CallEvent> implements ICallContext {
	
	protected final static Logger log = LoggerFactory.getLogger(CallContext.class);

    private String sessionId;
	private String aUri;
	private String bUri;
	private String callId;
    private String sipId;
    private boolean voice;
    private boolean video;
    private ICallApplication app;
    private IMediaContext mediaCtx;
    private Map<String, Object> data;
    private ICallContextDelegate stateChangedDelegate;

    public CallContext(String sessionId, String aUri, String bUri, boolean hasVoice, boolean hasVideo, String id)
	{
        this.sessionId = sessionId;
        this.aUri = aUri;
        this.bUri = bUri;
        this.voice = hasVoice;
        this.video = hasVideo;
		this.callId = id;
        this.state = CallState.INIT;
        this.data = new HashMap<String, Object>();
        super.setLogger(log);
	}

    public void initWith(ICallContextDelegate stateChangedDelegate){
        this.stateChangedDelegate = stateChangedDelegate;
    }

    public IMediaContext getMediaContext() {
        return mediaCtx;
    }

    public void setMediaContext(IMediaContext mediaCtx) {
        this.mediaCtx = mediaCtx;
    }

    public boolean hasVoice() {
        return voice;
    }

    public boolean hasVideo() {
        return video;
    }

    public String getSipId() {
        return sipId;
    }

    public void setSipId(String sipId) {
        this.sipId = sipId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void onEvent(CallEvent event)
    {
        switch(this.state)
        {
            case INIT:
                if(event.getType() == MessageType.START_CALL) {
                    setState(CallState.STARTING, event);
                } else if(event.getType() == MessageType.INCOMING_CALL) {
                    setState(CallState.STARTING_INCOMING, event);
                }else {
                    unhandledEvent(event);
                }
                break;

            case STARTING:
                if(event.getType() == MessageType.CALL_STARTED) {
                    setState(CallState.STARTED, event);
                } else if(event.getType() == MessageType.CALL_STARTING) {
                    setState(CallState.STARTING, event);
                } else if(event.getType() == MessageType.CALL_FAILED) {
                    setState(CallState.FAILED, event);
                } else if(event.getType() == MessageType.CALL_FINISHED) {
                    setState(CallState.FINISHED, event);
                } else if(event.getType() == MessageType.HANGUP_CALL) {
                    setState(CallState.FINISHED, event);
                } else {
                    unhandledEvent(event);
                }
                break;

            case STARTING_INCOMING:
                if(event.getType() == MessageType.CALL_STARTED) {
                    setState(CallState.STARTED, event);
                } else if(event.getType() == MessageType.ACCEPT_CALL) {
                    //setState(CallState.STARTED, event);
                } else if(event.getType() == MessageType.REJECT_CALL) {
                    setState(CallState.FINISHED, event);
                } else if(event.getType() == MessageType.CALL_FAILED) {
                    setState(CallState.FAILED, event);
                } else if(event.getType() == MessageType.CALL_FINISHED) {
                    setState(CallState.FINISHED, event);
                } else if(event.getType() == MessageType.HANGUP_CALL) {
                    setState(CallState.FINISHED, event);
                } else {
                    unhandledEvent(event);
                }
                break;

            case STARTED:
                if(event.getType() == MessageType.CALL_FINISHED) {
                    setState(CallState.FINISHED, event);
                } else if(event.getType() == MessageType.HANGUP_CALL) {
                    setState(CallState.FINISHED, event);
                } else
                    unhandledEvent(event);
                break;


            case FINISHED:
                unhandledEvent(event);
                break;
        }
    }

    @Override
    protected void setState(CallState state, CallEvent event) {
        super.setState(state, event);
        event.getMessage().set(AlenaMessage.PROP_CALL_ID, callId); // only for START_CALL

        switch(state)
        {
            case STARTING:
                stateChangedDelegate.onCallStarting(this, (AlenaMessage)event.getMessage());
                break;

            case STARTING_INCOMING:
                // save SDP from incoming call
                set(AlenaMessage.PROP_SDP, event.getMessage().get(AlenaMessage.PROP_SDP));
                stateChangedDelegate.onIncomingCall(this, (AlenaMessage)event.getMessage());
                break;

            case STARTED:
                stateChangedDelegate.onCallStarted(this, (AlenaMessage)event.getMessage());
                break;

            case FAILED:
                stateChangedDelegate.onCallFailed(this, (AlenaMessage)event.getMessage());
                break;

            case FINISHED:
                stateChangedDelegate.onCallFinished(this, (AlenaMessage)event.getMessage());
                break;
        }
    }

    public String getCallId() {
		return callId;
	}

	public String getA() {
		return aUri;
	}

	public String getB() {
		return bUri;
	}

    @Override
    public void set(String key, Object value){
        data.put(key, value);
    }

    @Override
    public Object get(String key){
        return data.get(key);
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    public void delete(String key) {
        data.remove(key);
    }

    public String toString(){
        return "[CallContext state="+state.name()+" id="+callId+"  sipId="+sipId+" sessionId="+sessionId+"]" ;
    }
}


