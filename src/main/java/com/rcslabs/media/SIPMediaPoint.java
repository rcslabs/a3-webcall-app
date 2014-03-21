package com.rcslabs.media;

import com.rcslabs.calls.ClientCapabilities;
import com.rcslabs.redis.IMessage;
import com.rcslabs.webcall.AlenaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SIPMediaPoint extends MediaPoint{

    protected final static Logger log = LoggerFactory.getLogger(SIPMediaPoint.class);

    public SIPMediaPoint(String sessionId, String callId, ClientCapabilities cc,
                          boolean hasVoice, boolean hasVideo) {
        super(sessionId, callId, cc, hasVoice, hasVideo, "internal");
        super.setLogger(log);
    }

    protected void onSdpOffererReceived(MediaEvent event){
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setOfferer((String)msg.get(AlenaMessage.PROP_SDP));
        enterStateDelegate.onSdpOffererReceived(this, (AlenaMessage)msg);
    }

    protected void onSdpAnswererReceived(MediaEvent event){
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setAnswerer((String) msg.get(AlenaMessage.PROP_SDP));
        msg.set(AlenaMessage.PROP_CALL_ID,  callId);
        msg.set(AlenaMessage.PROP_POINT_ID, pointId);
        msg.set(AlenaMessage.PROP_PROFILE, profile);
        enterStateDelegate.onSdpAnswererReceived(this, (AlenaMessage)msg);
    }

    public String toString(){
        return "[SIPMediaPoint pointId="+pointId+", state="+state+", sessionId="+sessionId+", callId="+callId+", profile="+profile+"]";
    }

}
