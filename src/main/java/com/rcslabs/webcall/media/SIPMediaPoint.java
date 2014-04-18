package com.rcslabs.webcall.media;

import com.rcslabs.webcall.calls.ClientCapabilities;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SIPMediaPoint extends MediaPoint{

    protected final static Logger log = LoggerFactory.getLogger(SIPMediaPoint.class);

    public SIPMediaPoint(String sessionId, String callId, ClientCapabilities cc,
                          boolean hasVoice, boolean hasVideo) {
        super(sessionId, callId, cc, hasVoice, hasVideo, "internal");
        super.setLogger(log);
    }

    protected void onSdpOffererReceived(MediaSignal event){
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setOfferer((String)msg.get(MessageProperty.SDP));
        enterStateDelegate.onSdpOffererReceived(this, msg);
    }

    protected void onSdpAnswererReceived(MediaSignal event){
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setAnswerer((String) msg.get(MessageProperty.SDP));
        msg.set(MessageProperty.CALL_ID,  callId);
        msg.set(MessageProperty.POINT_ID, pointId);
        msg.set(MessageProperty.PROFILE, profile);
        enterStateDelegate.onSdpAnswererReceived(this, msg);
    }

    public String toString(){
        return "[SIPMediaPoint pointId="+pointId+", state="+state+", sessionId="+sessionId+", callId="+callId+", profile="+profile+"]";
    }

}
