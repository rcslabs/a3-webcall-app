package com.rcslabs.media;

import com.rcslabs.calls.ClientCapabilities;
import com.rcslabs.messaging.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WRTCMediaPoint extends MediaPoint {

    protected final static Logger log = LoggerFactory.getLogger(WRTCMediaPoint.class);

    public WRTCMediaPoint(String sessionId, String callId, ClientCapabilities cc,
                      boolean hasVoice, boolean hasVideo) {
        super(sessionId, callId, cc, hasVoice, hasVideo, "external");
        super.setLogger(log);
    }

    protected void onSdpOffererReceived(MediaEvent event){
        // Now we get the source message
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setOfferer((String)msg.get(IMessage.PROP_SDP));
        // and set self properties into one for pretty publishing.
        msg.set(IMessage.PROP_CALL_ID,  callId);
        msg.set(IMessage.PROP_POINT_ID, pointId);
        msg.set(IMessage.PROP_PROFILE, profile);
        msg.set(IMessage.PROP_SESSION_ID, sessionId);
        enterStateDelegate.onSdpOffererReceived(this, msg);
    }

    protected void onSdpAnswererReceived(MediaEvent event){
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setAnswerer((String) msg.get(IMessage.PROP_SDP));
        msg.set(IMessage.PROP_CALL_ID,  callId);
        msg.set(IMessage.PROP_POINT_ID, pointId);
        msg.set(IMessage.PROP_PROFILE, profile);
        enterStateDelegate.onSdpAnswererReceived(this, msg);
    }

    public String toString(){
        return "[WRTCMediaPoint state="+state+", pointId="+pointId+", sessionId="+sessionId+", callId="+callId+", profile="+profile+"]";
    }
}