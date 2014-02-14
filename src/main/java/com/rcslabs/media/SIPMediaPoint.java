package com.rcslabs.media;

import com.rcslabs.calls.ClientCapabilities;
import com.rcslabs.messaging.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SIPMediaPoint extends MediaPoint{

    protected final static Logger log = LoggerFactory.getLogger(SIPMediaPoint.class);

    public SIPMediaPoint(String sessionId, String callId,
                          boolean hasVoice, boolean hasVideo) {
        super(sessionId, callId, null, hasVoice, hasVideo, "internal");
        super.setLogger(log);
        // TODO: Harcoded capabilities for SIP. Remove it in the future?
        cc = new ClientCapabilities();
        cc.setProfile("RTP/AVP");
        cc.addAudio("PCMA/8000");
        cc.addVideo("VP8/90000");
        cc.addVideo("H264/90000");
    }

    protected void onSdpOffererReceived(MediaEvent event){
        IMessage msg = event.getMessage().cloneWithSameType();
        sdp.setOfferer((String)msg.get(IMessage.PROP_SDP));
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
        return "[SIPMediaPoint pointId="+pointId+", state="+state+", sessionId="+sessionId+", callId="+callId+", profile="+profile+"]";
    }

}
