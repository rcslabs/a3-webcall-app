package com.rcslabs.webcall;

import com.rcslabs.a3.AbstractApplication;
import com.rcslabs.a3.auth.AuthMessage;
import com.rcslabs.a3.auth.CriticalFailedSession;
import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.config.ISipConfig;
import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.RedisConnector;
import com.rcslabs.a3.rtc.*;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.entity.*;
import com.rcslabs.webcall.calls.*;
import com.rcslabs.webcall.media.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class BaseCallApplication extends AbstractApplication implements
        ICallApplication, ICallContextDelegate, IMediaPointDelegate {

    protected IRclFactory factory;
    protected ICallListener jainSipCallListener;

    protected IAuthController authController;

    protected Map<String, IMediaPoint> points;
    protected Map<String, ICallContext> calls;
    protected Map<String, String> sipId2callId; // sip ID to call ID mapping

    protected CallLogger callLogger;

    public BaseCallApplication(RedisConnector redisConnector, String channelName, ICallAppConfig config, IRclFactory factory)
    {
        super(redisConnector, channelName, config);

        this.points = new ConcurrentHashMap<>();
        this.calls = new ConcurrentHashMap<>();
        this.sipId2callId = new ConcurrentHashMap<>();

        this.factory = factory;

        jainSipCallListener = new JainSipCallListener(this);
        authController = new SipAuthController("auth:sip", config, redisConnector, factory);

        callLogger = new CallLogger(redisConnector);
    }

    @Override
    public void onMessageReceived(IMessage message)
    {
        try {
            // all messages except START_SESSION must contains parameter "sessionId"
            // validate it and throws an Exception unsuccessfully

            if(!message.has(MessageProperty.SESSION_ID)){
                if(message.getType() != AuthMessage.Type.START_SESSION){
                    throw new InvalidMessageException("Skip the message without sessionId " + message);
                }
            } else {
                String sessionId = (String)message.get(MessageProperty.SESSION_ID);
                ISession session = findSession(sessionId);
                if(null == session){
                    throw new InvalidMessageException("Session not found " + sessionId);
                }
            }

            if(message instanceof AuthMessage)
                handleAuthMessage((AuthMessage) message);
            else if(message instanceof CallMessage)
                handleCallMessage((CallMessage) message);
            else if(message instanceof MediaMessage)
                handleMediaMessage((MediaMessage)message);
            else
                log.warn("Unhandled message " + message.getType());
        } catch (Exception e) {
            handleOnMessageException(message, e);
        }
    }

    protected void handleAuthMessage(AuthMessage message)
            throws Exception
    {

        switch (message.getType()) {
            // auth messages
            case START_SESSION:
                beforeStartSession(message); 
            case CLOSE_SESSION:
                ((SipAuthController)authController).onMessageReceived(message);
                // TODO: redisConnector.publish("auth:sip", message);
                break;
        }
    }

    protected void handleCallMessage(CallMessage message)
            throws Exception
    {
        ICallContext ctx;
        String callId;

        switch (message.getType())
        {
            // calls messages
            case START_CALL:
                ctx = createCallContext(message);
                // create first WRTC media point
                createWRTCMediaPoint(ctx.getSessionId(), ctx.getCallId(),
                        new ClientCapabilities(message.get(MessageProperty.CLIENT_CAPABILITIES)), ctx.hasVoice(), ctx.hasVideo()
                );
                ctx.onEvent(new CallSignal(message));
                break;

            case INCOMING_CALL:
                ctx = createIncomingCallContext(message);
                ctx.onEvent(new CallSignal(message));
                break;

            case CALL_STARTING:
            case CALL_STARTED:
            case CALL_FINISHED:
            case HANGUP_CALL:
                callId = (String) message.get(MessageProperty.CALL_ID);
                ctx = findCallContext(callId);
                if(null != ctx) {
                    ctx.onEvent(new CallSignal(message));
                }else{
                    log.warn("Call " + callId + " not found");
                }
                break;

            case ACCEPT_CALL:
                // TODO: case REJECT_CALL:
                callId = (String) message.get(MessageProperty.CALL_ID);
                ctx = findCallContext(callId);
                if(null != ctx) {
                    ctx.onEvent(new CallSignal(message));
                }else{
                    log.warn("Call " + callId + " not found");
                }
                // create WRTC media point (anyway) for abonent B at first
                createWRTCMediaPoint(ctx.getSessionId(), ctx.getCallId(),
                        new ClientCapabilities(message.get(MessageProperty.CLIENT_CAPABILITIES)), ctx.hasVoice(), ctx.hasVideo()
                );
                break;

            case SEND_DTMF:
                sendDTMF((String) message.get(MessageProperty.CALL_ID), (String) message.get(MessageProperty.DTMF));
                break;
        }
    }

    protected void handleMediaMessage(MediaMessage message)
            throws Exception
    {
        switch (message.getType())
        {
            case SDP_OFFER:
            case SDP_ANSWER:
            case CREATE_MEDIA_POINT_OK:
            case CREATE_MEDIA_POINT_FAILED:
            case CRITICAL_ERROR:
            case JOIN_OK:
            case JOIN_FAILED:
            case UNJOIN_OK:
            case UNJOIN_FAILED:
            case REMOVE_MEDIA_POINT_OK:
            case REMOVE_MEDIA_POINT_FAILED:
                String pointId = (String) message.get(MessageProperty.POINT_ID);
                IMediaPoint mp = findMediaPoint(pointId);
                if(null != mp){
                    mp.onEvent(new MediaSignal(message));
                }else{
                    log.warn("MediaPoint " + pointId + " not found");
                }
                break;
        }
    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e){
        log.error(e.getMessage(), e);

        if(message.getType() == AuthMessage.Type.START_SESSION){
            authController.onSessionFailed(new CriticalFailedSession(message), "Critical error");
        }
    }

    @Override
    public ISession findSession(String value) {
        return authController.findSession(value);
    }

    @Override
    public void beforeStartSession(IMessage message) throws Exception{}


    @Override
    public ICallContext createCallContext(IMessage message)    // TODO: for incoming call
    {
        try {

            String sipAddr = ((ISipConfig)config).getSipServerHost();
            if(0 != ((ISipConfig)config).getSipServerPort() && 5060 != ((ISipConfig)config).getSipServerPort()){
                sipAddr += (":"+((ISipConfig)config).getSipServerPort());
            }
            ISession session = findSession((String) message.get(MessageProperty.SESSION_ID));

            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            String callId = UUID.randomUUID().toString();
            String aUri = session.getUsername();
            String bUri = ((String)message.get(MessageProperty.B_URI));

            if(aUri.matches("^\\d+$")){ aUri = "sip:"+aUri+"@"+sipAddr; }
            if(bUri.matches("^\\d+$")){ bUri = "sip:"+bUri+"@"+sipAddr; }

            List<Object> vv = (List<Object>) message.get(MessageProperty.VOICE_VIDEO);
            ICallContext ctx = new CallContext(sessionId, aUri, bUri, (Boolean)vv.get(0), (Boolean)vv.get(1), callId);
            ctx.initWith(this);
            ctx.setMediaContext(new StaticMediaContext((ICallAppConfig)config));

            if(!calls.containsKey(callId)){
                calls.put(callId, ctx);
                return ctx;
            }else{
                throw new Exception("Call "+callId+" already exists");
            }
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public ICallContext createIncomingCallContext(IMessage message)
    {
        try {
            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            String callId =    (String)message.get(MessageProperty.CALL_ID);
            String aUri =      (String)message.get(MessageProperty.A_URI);
            String bUri =      (String)message.get(MessageProperty.B_URI);
            String sdp =       (String)message.get(MessageProperty.SDP);

            // analyze SDP and set 'vv' properties
            boolean hasVoice = Pattern.compile("^m=audio", Pattern.MULTILINE).matcher(sdp).find();
            boolean hasVideo = Pattern.compile("^m=video", Pattern.MULTILINE).matcher(sdp).find();
            ICallContext ctx = new CallContext(sessionId, aUri, bUri, hasVoice, hasVideo, callId);
            ctx.initWith(this);

            if(!calls.containsKey(callId)){
                calls.put(callId, ctx);
                return ctx;
            }else{
                throw new Exception("Call "+callId+" already exists");
            }
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void startCall(ICallContext ctx, String sdpOfferer)
    {
        String aUri = ctx.getA();
        String bUri = ctx.getB();
        String jsipConnId = findSession(ctx.getSessionId()).getSessionId();

        log.info("Start SIP call "+ctx.getCallId()+" "+aUri+" -> "+bUri);

        try{
            ICall call = factory.findConnection(jsipConnId).getService(ITelephonyService.class).newCall();
            ICallParams params = new CallParams();
            params.setFrom(aUri);
            params.setTo(bUri);
            params.setCallType(ctx.hasVideo() ? CallType.VIDEO : CallType.AUDIO);

            List<ICallParameter> ctxCallParams = ((CallContext)ctx).getCallParams();
            for(ICallParameter p : ctxCallParams){
                params.addParameter(p);
            }

            ISdpObject sdp = new SdpObject();
            sdp.setOfferer(sdpOfferer);
            params.setSdpObject(sdp);
            call.addListener(jainSipCallListener);
            call.start(params);
            ctx.setSipId(call.getId());
            sipId2callId.put(ctx.getSipId(), ctx.getCallId());
            callLogger.push(new CallLogEntry(CallMessage.Type.START_CALL, ctx));
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ICallContext findCallContext(String callId){
        // resolve callId by sipId
        if(sipId2callId.containsKey(callId)){ callId = sipId2callId.get(callId); }
        if(!calls.containsKey(callId)){ return null; }
        return calls.get(callId);
    }


    @Override
    public IMediaPoint createWRTCMediaPoint(
            String sessionId, String callId, ClientCapabilities cc, boolean hasVoice, boolean hasVideo) {

        IMediaPoint mp = new WRTCMediaPoint(sessionId, callId, cc, hasVoice, hasVideo);
        mp.initWith(this);
        mp.setMediaContext(calls.get(callId).getMediaContext());
        points.put(mp.getPointId(), mp);

        IMessage message = new MediaMessage(MediaMessage.Type.CREATE_MEDIA_POINT);
        message.set(MessageProperty.CLIENT_CAPABILITIES, mp.getClientCapabilities().getRawData());
        message.set(MessageProperty.VOICE_VIDEO, Arrays.asList(hasVoice, hasVideo));
        message.set(MessageProperty.POINT_ID, mp.getPointId());
        message.set(MessageProperty.PROFILE, mp.getProfile());
        message.set(MessageProperty.SENDER, getChannel());
        message.set(MessageProperty.SESSION_ID, sessionId);
        redisConnector.publish(mp.getMediaContext().getMcChannel(), message);

        return mp;
    }

    @Override
    public IMediaPoint createSIPMediaPoint(
            String sessionId, String callId, boolean hasVoice, boolean hasVideo) {

        IMediaContext media = calls.get(callId).getMediaContext();
        ClientCapabilities cc = new ClientCapabilities();
        cc.setProfile("RTP/AVP");
        Set<String> audioCodecs = media.getRtpAudioCodecs();
        for(String c : audioCodecs){ cc.addAudio(c); }
        Set<String> videoCodecs = media.getRtpAudioCodecs();
        for(String c : videoCodecs){ cc.addVideo(c); }

        IMediaPoint mp = new SIPMediaPoint(sessionId, callId, cc, hasVoice, hasVideo);
        mp.initWith(this);
        mp.setMediaContext(media);
        points.put(mp.getPointId(), mp);

        IMessage message = new MediaMessage(MediaMessage.Type.CREATE_MEDIA_POINT);
        message.set(MessageProperty.CLIENT_CAPABILITIES, mp.getClientCapabilities().getRawData());
        message.set(MessageProperty.VOICE_VIDEO, Arrays.asList(hasVoice, hasVideo));
        message.set(MessageProperty.POINT_ID, mp.getPointId());
        message.set(MessageProperty.PROFILE, mp.getProfile());
        message.set(MessageProperty.SENDER, getChannel());
        message.set(MessageProperty.SESSION_ID, sessionId);
        message.set("dtmf", true);
        redisConnector.publish(media.getMcChannel(), message);

        return mp;
    }

    @Override
    public void removeMediaPoint(String pointId) {
        IMediaPoint mp = findMediaPoint(pointId);
        if(mp == null){ return; /* skip */ }
        points.remove(pointId);

        IMessage message = new MediaMessage(MediaMessage.Type.REMOVE_MEDIA_POINT);
        message.set(MessageProperty.POINT_ID, pointId);
        message.set(MessageProperty.SENDER, getChannel());
        message.set(MessageProperty.SESSION_ID, mp.getSessionId());
        redisConnector.publish(mp.getMediaContext().getMcChannel(), message);
    }

    @Override
    public void joinRoom(String pointId, String roomId) {
        IMediaPoint mp = findMediaPoint(pointId);
        if(mp == null){ return; /* skip */ }

        IMessage message = new MediaMessage(MediaMessage.Type.JOIN_ROOM);
        message.set(MessageProperty.POINT_ID, pointId);
        message.set(MessageProperty.ROOM_ID, roomId);
        message.set(MessageProperty.SENDER, getChannel());
        message.set(MessageProperty.SESSION_ID, mp.getSessionId());
        redisConnector.publish(mp.getMediaContext().getMcChannel(), message);
        // TODO: This is workaround, we have no JOIN_OK in media-controller
        mp.onEvent(new MediaSignal(MediaMessage.Type.JOIN_OK));
    }

    @Override
    public void unjoinRoom(String pointId, String roomId) {
        IMediaPoint mp = findMediaPoint(pointId);
        if(mp == null){ return; /* skip */ }

        IMessage message = new MediaMessage(MediaMessage.Type.UNJOIN_ROOM);
        message.set(MessageProperty.POINT_ID, pointId);
        message.set(MessageProperty.ROOM_ID, roomId);
        message.set(MessageProperty.SENDER, getChannel());
        message.set(MessageProperty.SESSION_ID, mp.getSessionId());
        redisConnector.publish(mp.getMediaContext().getMcChannel(), message);
        // TODO: This is workaround, we have no UNJOIN_OK in media-controller
        mp.onEvent(new MediaSignal(MediaMessage.Type.UNJOIN_OK));
    }

    @Override
    public void stopMedia(String callId) {
        for(IMediaPoint p : points.values()){
            if(p.getCallId().equals(callId)){
                if(p.getState() == IMediaPoint.MediaPointState.JOINED){
                    unjoinRoom(p.getPointId(), p.getCallId());
                }
                removeMediaPoint(p.getPointId());
            }
        }
    }

    @Override
    public void sendDTMF(String callId, String dtmf) {
        for (IMediaPoint item : points.values()) {
            if (item.getCallId().equals(callId)) {
                if(item instanceof SIPMediaPoint){
                    IMessage message = new CallMessage(CallMessage.Type.SEND_DTMF);
                    message.set(MessageProperty.POINT_ID, item.getPointId());
                    message.set(MessageProperty.DTMF, dtmf);
                    redisConnector.publish(item.getMediaContext().getMcChannel(), message);
                }
            }
        }
    }

    @Override
    public IMediaPoint findMediaPoint(String pointId){
        if(points.containsKey(pointId)){
            return points.get(pointId);
        }
        return null;
    }

    @Override
    public List<IMediaPoint> findMediaPointsByCallId(String callId)
    {
        ArrayList<IMediaPoint> mps = new ArrayList<IMediaPoint>();
        for (IMediaPoint item : points.values()) {
            if (item.getCallId().equals(callId)) {
                mps.add(item);
            }
        }
        return mps;
    }

    private void removeCallContext(String callId){

        ICallContext ctx = findCallContext(callId);
        if(ctx == null){ /* skip */ return; }

        try {
            ICall call = factory.findConnection(ctx.getSessionId())
                    .getService(ITelephonyService.class)
                    .findCall(ctx.getSipId());

            if(null != call){
                call.finish();
                call.removeListener(jainSipCallListener);
            }
        } catch (ServiceNotEnabledException e) {
            log.error(e.getMessage());
        }

        calls.remove(callId);
        for(Map.Entry<String, String> e : sipId2callId.entrySet()){
            if(e.getValue().equals(callId)){
                sipId2callId.remove(e.getKey());
            }
        }
    }








    @Override
    public void onCallStarting(ICallContext ctx, IMessage message) {
        redisConnector.publish(message.getClientChannel(), message.cloneWithAnyType(CallMessage.Type.CALL_STARTING));
        callLogger.push(new CallLogEntry(CallMessage.Type.CALL_STARTING, ctx));
    }

    @Override
    public void onIncomingCall(ICallContext ctx, IMessage message) {
        IMessage message2 = message.cloneWithSameType();
        message2.delete(MessageProperty.SDP);
        List<Boolean> vv = new ArrayList<Boolean>();
        vv.add(ctx.hasVoice());
        vv.add(ctx.hasVideo());
        message2.set(MessageProperty.VOICE_VIDEO, vv);
        redisConnector.publish(message.getClientChannel(), message2);
    }

    @Override
    public void onCallStarted(ICallContext ctx, IMessage message) {
        redisConnector.publish(message.getClientChannel(), message.cloneWithSameType());
        callLogger.push(new CallLogEntry(CallMessage.Type.CALL_STARTED, ctx));
        // it was a SIP call? Find media point and set state
        MediaMessage sipSdpAnswerMessage = (MediaMessage)(message.cloneWithAnyType(MediaMessage.Type.SDP_ANSWER));
        for(IMediaPoint p : points.values()){
            if(p.getCallId().equals(message.get(MessageProperty.CALL_ID))
                    && p.getState() == IMediaPoint.MediaPointState.OFFERER_RECEIVED){
                // ok, this is media point waiting SDP answer?
                p.onEvent(new MediaSignal(sipSdpAnswerMessage)); break;
            }
        }
    }

    @Override
    public void onCallFinished(ICallContext ctx, IMessage message) {
        redisConnector.publish(message.getClientChannel(), message.cloneWithAnyType(CallMessage.Type.CALL_FINISHED));
        callLogger.push(new CallLogEntry(CallMessage.Type.CALL_FINISHED, ctx));
        stopMedia(ctx.getCallId());
        removeCallContext(ctx.getCallId());
    }

    @Override
    public void onCallFailed(ICallContext ctx, IMessage message) {
        redisConnector.publish(message.getClientChannel(), message.cloneWithSameType());
        callLogger.push(new CallLogEntry(CallMessage.Type.CALL_FAILED, ctx, ""+message.get(MessageProperty.REASON)));
        stopMedia(ctx.getCallId());
        removeCallContext(ctx.getCallId());
    }











    @Override
    public void onSdpOffererReceived(IMediaPoint mp, IMessage message) {
        // WRTC - send to client
        if(mp instanceof WRTCMediaPoint){
            redisConnector.publish(message.getClientChannel(), message);

        }else if(mp instanceof SIPMediaPoint){
            ICallContext ctx = findCallContext(mp.getCallId());
            if(null == ctx){
                log.error("Call "+mp.getCallId()+" not found");
                IMessage message2 = new CallMessage(CallMessage.Type.CALL_FAILED);
                redisConnector.publish(message.getClientChannel(), message2);
                return;
            }
            startCall(ctx, (String)message.get(MessageProperty.SDP));
        }
    }

    @Override
    public void onSdpAnswererReceived(IMediaPoint mp, IMessage message) {
        message.set(MessageProperty.SENDER, getChannel());
        redisConnector.publish(mp.getMediaContext().getMcChannel(), message);
    }

    @Override
    public void onMediaPointCreated(IMediaPoint mp, IMessage message) {
        // TODO: now simple logic for outgoing call to SIP
        if(mp instanceof WRTCMediaPoint){
            createSIPMediaPoint(mp.getSessionId(), mp.getCallId(), mp.hasVoice(), mp.hasVideo());

        }else if(mp instanceof SIPMediaPoint){
            ICallContext ctx = findCallContext(mp.getCallId());
            if(null == ctx){
                log.error("Call "+mp.getCallId()+" not found");
                IMessage message2 = new CallMessage(CallMessage.Type.CALL_FAILED);
                redisConnector.publish(message.getClientChannel(), message2);
                return;
            }

            List<IMediaPoint> mps = findMediaPointsByCallId(mp.getCallId());
            for(IMediaPoint item : mps){
                joinRoom(item.getPointId(), item.getCallId());
            }
        }
    }

    @Override
    public void onMediaPointJoinedToRoom(IMediaPoint mp, IMessage message) { }

    @Override
    public void onMediaPointUnjoinedFromRoom(IMediaPoint mp, IMessage message) { }

    @Override
    public void onMediaFailed(IMediaPoint mp, IMessage message) {
        ICallContext ctx = findCallContext(mp.getCallId());
        if(null != ctx){
            IMessage msg = new CallMessage(CallMessage.Type.CALL_FAILED);
            msg.set(MessageProperty.SESSION_ID, ctx.getSessionId());
            msg.set(MessageProperty.CALL_ID, ctx.getCallId());
            msg.set(MessageProperty.REASON, "" + message.getType());
            onCallFailed(ctx, msg);
        }
    }
}
