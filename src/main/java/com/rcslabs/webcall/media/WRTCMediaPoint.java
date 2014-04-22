package com.rcslabs.webcall.media;

import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.webcall.MessageProperty;
import com.rcslabs.webcall.calls.ClientCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WRTCMediaPoint extends MediaPoint {

    protected final static Logger log = LoggerFactory.getLogger(WRTCMediaPoint.class);

    public WRTCMediaPoint(String sessionId, String callId, ClientCapabilities cc,
                      boolean hasVoice, boolean hasVideo) {
        super(sessionId, callId, cc, hasVoice, hasVideo, "external");
        super.setLogger(log);
    }

    protected void onSdpOffererReceived(MediaSignal event){
        // Now we get the source message
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setOfferer((String)msg.get(MessageProperty.SDP));
        // and set self properties into one for pretty publishing.
        msg.set(MessageProperty.CALL_ID,  callId);
        msg.set(MessageProperty.POINT_ID, pointId);
        msg.set(MessageProperty.PROFILE, profile);
        msg.set(MessageProperty.SESSION_ID, sessionId);
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
        return "[WRTCMediaPoint state="+state+", pointId="+pointId+", sessionId="+sessionId+", callId="+callId+", profile="+profile+"]";
    }
}