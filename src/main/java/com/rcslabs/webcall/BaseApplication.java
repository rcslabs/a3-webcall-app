package com.rcslabs.webcall;

import com.rcslabs.auth.*;
import com.rcslabs.calls.*;
import com.rcslabs.media.*;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.entity.*;
import com.rcslabs.redis.IMessage;
import com.rcslabs.redis.IMessageBroker;
import com.rcslabs.redis.IMessageBrokerDelegate;
import com.rcslabs.redis.ITypedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class BaseApplication implements
        ICallApplication, ICallContextDelegate, IMediaPointDelegate, IMessageBrokerDelegate {

    protected final static Logger log = LoggerFactory.getLogger(BaseApplication.class);

    protected String channelName;

    protected IConfig config;
    protected IRclFactory factory;
    protected ICallListener jainSipCallListener;
    protected IMessageBroker broker;
    protected IAuthController authController;

    protected Map<String, IMediaPoint> points;
    protected Map<String, ICallContext> calls;
    protected Map<String, String> sipId2callId; // sip ID to call ID mapping


    public BaseApplication(String channelName, IConfig config, IMessageBroker broker, IRclFactory factory)
    {
        this.channelName = channelName;
        this.points = new ConcurrentHashMap<String, IMediaPoint>();
        this.calls = new ConcurrentHashMap<String, ICallContext>();
        this.sipId2callId = new ConcurrentHashMap<String, String>();

        this.config = config;
        this.factory = factory;
        this.broker = broker;

        jainSipCallListener = new JainSipCallListener(this);
        authController = new SipAuthController(config, broker, factory);
    }

    @Override
    public boolean ready(){
        return true;
    }

    protected void validateMessage(ITypedMessage<MessageType> message) throws Exception
    {
        if(message.getType() == MessageType.START_SESSION){ return; }

        // all messages except START_SESSION must contains parameter "sessionId"
        // validate it and throw Exception unsuccessfully

        if(!message.has(AlenaMessage.PROP_SESSION_ID)){
            throw new Exception("Skip the message without sessionId " + message);
        } else {
            String sessionId = (String)message.get(AlenaMessage.PROP_SESSION_ID);
            ISession session = authController.findSession(sessionId);
            if(null == session){
                log.warn("Session for message not found " + message);
            }
        }
    }


    @Override
    public void onMessageReceived(String channel, IMessage message0)
    {
        AlenaMessage message = (AlenaMessage)message0;

        try {
            validateMessage(message);

            ICallContext ctx;
            String callId;

            switch (message.getType())
            {
                // auth messages
                case START_SESSION:
                    beforeStartSession(message);
                case CLOSE_SESSION:
                    authController.onMessageReceived(channel, message);
                    break;

                // calls messages
                case START_CALL:
                    ctx = createCallContext(message);
                    // create first WRTC media point
                    createWRTCMediaPoint(ctx.getSessionId(), ctx.getCallId(),
                            new ClientCapabilities(message.get(AlenaMessage.PROP_CC)), ctx.hasVoice(), ctx.hasVideo()
                    );
                    ctx.onEvent(new CallEvent(message));
                    break;

                case INCOMING_CALL:
                    ctx = createIncomingCallContext(message);
                    ctx.onEvent(new CallEvent(message));
                    break;

                case CALL_STARTING:
                case CALL_STARTED:
                case CALL_FINISHED:
                case HANGUP_CALL:
                    callId = (String) message.get(AlenaMessage.PROP_CALL_ID);
                    ctx = findCallContext(callId);
                    if(null != ctx) {
                        ctx.onEvent(new CallEvent(message));
                    }else{
                        log.warn("Call " + callId + " not found");
                    }
                    break;

                case ACCEPT_CALL:
                    // TODO: case REJECT_CALL:
                    callId = (String) message.get(AlenaMessage.PROP_CALL_ID);
                    ctx = findCallContext(callId);
                    if(null != ctx) {
                        ctx.onEvent(new CallEvent(message));
                    }else{
                        log.warn("Call " + callId + " not found");
                    }
                    // create WRTC media point (anyway) for abonent B at first
                    createWRTCMediaPoint(ctx.getSessionId(), ctx.getCallId(),
                            new ClientCapabilities(message.get(AlenaMessage.PROP_CC)), ctx.hasVoice(), ctx.hasVideo()
                    );
                    break;

                case SEND_DTMF:
                    sendDTMF((String) message.get(AlenaMessage.PROP_CALL_ID), (String) message.get(AlenaMessage.PROP_DTMF));
                    break;

                //   media messages
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
                    String pointId = (String) message.get(AlenaMessage.PROP_POINT_ID);
                    IMediaPoint mp = findMediaPoint(pointId);
                    if(null != mp){
                        mp.onEvent(new MediaEvent(message));
                    }else{
                        log.warn("MediaPoint " + pointId + " not found");
                    }
                    break;

                default:
                    log.warn("Unhandled message " + message.getType());
            }

        } catch (Exception e) {
            handleOnMessageException(message, e);
        }
    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e){
        ((AlenaMessage)message).cancel();
        log.error(e.getMessage(), e);

        if(((AlenaMessage)message).getType() == MessageType.START_SESSION){
            ((IAuthControllerDelegate)authController)
                    .onSessionFailed(new CriticalFailedSession(((AlenaMessage)message)), "Critical error");
        }
    }



    @Override
    public IConfig getConfig() {
        return config;
    }

    @Override
    public IAuthController getAuthController() {
        return authController;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }


    @Override
    public void beforeStartSession(AlenaMessage message) throws Exception{}


    @Override
    public ICallContext createCallContext(AlenaMessage message)    // TODO: for incoming call
    {
        try {

            String sipAddr = config.getSipServerHost();
            if(0 != config.getSipServerPort() && 5060 != config.getSipServerPort()){
                sipAddr += (":"+config.getSipServerPort());
            }
            ISession session = getAuthController().findSession((String)message.get(AlenaMessage.PROP_SESSION_ID));

            String sessionId = (String)message.get(AlenaMessage.PROP_SESSION_ID);
            String callId = UUID.randomUUID().toString();
            String aUri = session.getUsername();
            String bUri = ((String)message.get(AlenaMessage.PROP_B_URI));

            if(aUri.matches("^\\d+$")){ aUri = "sip:"+aUri+"@"+sipAddr; }
            if(bUri.matches("^\\d+$")){ bUri = "sip:"+bUri+"@"+sipAddr; }

            List<Object> vv = (List<Object>) message.get(AlenaMessage.PROP_VV);
            ICallContext ctx = new CallContext(sessionId, aUri, bUri, (Boolean)vv.get(0), (Boolean)vv.get(1), callId);
            ctx.initWith(this);
            ctx.setMediaContext(new StaticMediaContext(config));

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
            String sessionId = (String)message.get(AlenaMessage.PROP_SESSION_ID);
            String callId =    (String)message.get(AlenaMessage.PROP_CALL_ID);
            String aUri =      (String)message.get(AlenaMessage.PROP_A_URI);
            String bUri =      (String)message.get(AlenaMessage.PROP_B_URI);
            String sdp =       (String)message.get(AlenaMessage.PROP_SDP);

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
        String jsipConnId = getAuthController().findSession(ctx.getSessionId()).getSessionId();

        log.info("Start SIP call "+ctx.getCallId()+" "+aUri+" -> "+bUri);

        try{
            ICall call = factory.findConnection(jsipConnId).getService(ITelephonyService.class).newCall();
            ICallParams params = new CallParams();
            params.setFrom(aUri);
            params.setTo(bUri);
            params.setCallType(ctx.hasVideo() ? CallType.VIDEO : CallType.AUDIO);
            ISdpObject sdp = new SdpObject();
            sdp.setOfferer(sdpOfferer);
            params.setSdpObject(sdp);
            call.addListener(jainSipCallListener);
            call.start(params);
            ctx.setSipId(call.getId());
            sipId2callId.put(ctx.getSipId(), ctx.getCallId());
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

        IMessage message = new AlenaMessage(MessageType.CREATE_MEDIA_POINT);
        message.set(AlenaMessage.PROP_CC, mp.getClientCapabilities().getRawData());
        message.set(AlenaMessage.PROP_VV, Arrays.asList(hasVoice, hasVideo));
        message.set(AlenaMessage.PROP_POINT_ID, mp.getPointId());
        message.set(AlenaMessage.PROP_PROFILE, mp.getProfile());
        message.set(AlenaMessage.PROP_SENDER, getChannelName());
        message.set(AlenaMessage.PROP_SESSION_ID, sessionId);
        broker.publish(mp.getMediaContext().getMcChannel(), message);

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

        IMessage message = new AlenaMessage(MessageType.CREATE_MEDIA_POINT);
        message.set(AlenaMessage.PROP_CC, mp.getClientCapabilities().getRawData());
        message.set(AlenaMessage.PROP_VV, Arrays.asList(hasVoice, hasVideo));
        message.set(AlenaMessage.PROP_POINT_ID, mp.getPointId());
        message.set(AlenaMessage.PROP_PROFILE, mp.getProfile());
        message.set(AlenaMessage.PROP_SENDER, getChannelName());
        message.set(AlenaMessage.PROP_SESSION_ID, sessionId);
        message.set("dtmf", true);
        broker.publish(media.getMcChannel(), message);

        return mp;
    }

    @Override
    public void removeMediaPoint(String pointId) {
        IMediaPoint mp = findMediaPoint(pointId);
        if(mp == null){ return; /* skip */ }
        points.remove(pointId);

        IMessage message = new AlenaMessage(MessageType.REMOVE_MEDIA_POINT);
        message.set(AlenaMessage.PROP_POINT_ID, pointId);
        message.set(AlenaMessage.PROP_SENDER, getChannelName());
        message.set(AlenaMessage.PROP_SESSION_ID, mp.getSessionId());
        broker.publish(mp.getMediaContext().getMcChannel(), message);
    }

    @Override
    public void joinRoom(String pointId, String roomId) {
        IMediaPoint mp = findMediaPoint(pointId);
        if(mp == null){ return; /* skip */ }

        IMessage message = new AlenaMessage(MessageType.JOIN_ROOM);
        message.set(AlenaMessage.PROP_POINT_ID, pointId);
        message.set(AlenaMessage.PROP_ROOM_ID, roomId);
        message.set(AlenaMessage.PROP_SENDER, getChannelName());
        message.set(AlenaMessage.PROP_SESSION_ID, mp.getSessionId());
        broker.publish(mp.getMediaContext().getMcChannel(), message);
        // TODO: This is workaround, we have no JOIN_OK in media-controller
        mp.onEvent(new MediaEvent(MessageType.JOIN_OK));
    }

    @Override
    public void unjoinRoom(String pointId, String roomId) {
        IMediaPoint mp = findMediaPoint(pointId);
        if(mp == null){ return; /* skip */ }

        IMessage message = new AlenaMessage(MessageType.UNJOIN_ROOM);
        message.set(AlenaMessage.PROP_POINT_ID, pointId);
        message.set(AlenaMessage.PROP_ROOM_ID, roomId);
        message.set(AlenaMessage.PROP_SENDER, getChannelName());
        message.set(AlenaMessage.PROP_SESSION_ID, mp.getSessionId());
        broker.publish(mp.getMediaContext().getMcChannel(), message);
        // TODO: This is workaround, we have no UNJOIN_OK in media-controller
        mp.onEvent(new MediaEvent(MessageType.UNJOIN_OK));
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
                    IMessage message = new AlenaMessage(MessageType.SEND_DTMF);
                    message.set(AlenaMessage.PROP_POINT_ID, item.getPointId());
                    message.set(AlenaMessage.PROP_DTMF, dtmf);
                    broker.publish(item.getMediaContext().getMcChannel(), message);
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
    public void onCallStarting(ICallContext ctx, AlenaMessage message) {
        broker.publish(message.getClientChannel(), message.cloneWithAnyType(MessageType.CALL_STARTING));
    }

    @Override
    public void onIncomingCall(ICallContext ctx, AlenaMessage message) {
        IMessage message2 = message.cloneWithSameType();
        message2.delete(AlenaMessage.PROP_SDP);
        List<Boolean> vv = new ArrayList<Boolean>();
        vv.add(ctx.hasVoice());
        vv.add(ctx.hasVideo());
        message2.set(AlenaMessage.PROP_VV, vv);
        broker.publish(message.getClientChannel(), message2);
    }

    @Override
    public void onCallStarted(ICallContext ctx, AlenaMessage message) {
        broker.publish(message.getClientChannel(), message.cloneWithSameType());

        // it was a SIP call? Find media point and set state
        IMessage sipSdpAnswerMessage = message.cloneWithAnyType(MessageType.SDP_ANSWER);
        for(IMediaPoint p : points.values()){
            if(p.getCallId().equals(message.get(AlenaMessage.PROP_CALL_ID))
                    && p.getState() == IMediaPoint.MediaPointState.OFFERER_RECEIVED){
                // ok, this is media point waiting SDP answer?
                p.onEvent(new MediaEvent((AlenaMessage)sipSdpAnswerMessage)); break;
            }
        }
    }

    @Override
    public void onCallFinished(ICallContext ctx, AlenaMessage message) {
        broker.publish(message.getClientChannel(), message.cloneWithAnyType(MessageType.CALL_FINISHED));
        stopMedia(ctx.getCallId());
        removeCallContext(ctx.getCallId());
    }

    @Override
    public void onCallFailed(ICallContext ctx, AlenaMessage message) {
        broker.publish(message.getClientChannel(), message.cloneWithSameType());
        stopMedia(ctx.getCallId());
        removeCallContext(ctx.getCallId());
    }











    @Override
    public void onSdpOffererReceived(IMediaPoint mp, AlenaMessage message) {
        // WRTC - send to client
        if(mp instanceof WRTCMediaPoint){
            broker.publish(message.getClientChannel(), message);

        }else if(mp instanceof SIPMediaPoint){
            ICallContext ctx = findCallContext(mp.getCallId());
            if(null == ctx){
                log.error("Call "+mp.getCallId()+" not found");
                IMessage message2 = new AlenaMessage(MessageType.CALL_FAILED);
                broker.publish(message.getClientChannel(), message2);
                return;
            }
            startCall(ctx, (String)message.get(AlenaMessage.PROP_SDP));
        }
    }

    @Override
    public void onSdpAnswererReceived(IMediaPoint mp, AlenaMessage message) {
        message.set(AlenaMessage.PROP_SENDER, getChannelName());
        broker.publish(mp.getMediaContext().getMcChannel(), message);
    }

    @Override
    public void onMediaPointCreated(IMediaPoint mp, AlenaMessage message) {
        // TODO: now simple logic for outgoing call to SIP
        if(mp instanceof WRTCMediaPoint){
            createSIPMediaPoint(mp.getSessionId(), mp.getCallId(), mp.hasVoice(), mp.hasVideo());

        }else if(mp instanceof SIPMediaPoint){
            ICallContext ctx = findCallContext(mp.getCallId());
            if(null == ctx){
                log.error("Call "+mp.getCallId()+" not found");
                IMessage message2 = new AlenaMessage(MessageType.CALL_FAILED);
                broker.publish(message.getClientChannel(), message2);
                return;
            }

            List<IMediaPoint> mps = findMediaPointsByCallId(mp.getCallId());
            for(IMediaPoint item : mps){
                joinRoom(item.getPointId(), item.getCallId());
            }
        }
    }

    @Override
    public void onMediaPointJoinedToRoom(IMediaPoint mp, AlenaMessage message) { }

    @Override
    public void onMediaPointUnjoinedFromRoom(IMediaPoint mp, AlenaMessage message) { }

    @Override
    public void onMediaFailed(IMediaPoint mp, AlenaMessage message) {
        ICallContext ctx = findCallContext(mp.getCallId());
        if(null != ctx){
            AlenaMessage msg = new AlenaMessage(MessageType.CALL_FAILED);
            msg.set(AlenaMessage.PROP_SESSION_ID, ctx.getSessionId());
            msg.set(AlenaMessage.PROP_CALL_ID, ctx.getCallId());
            msg.set(AlenaMessage.PROP_REASON, "" + message.getType());
            onCallFailed(ctx, msg);
        }
    }
}
