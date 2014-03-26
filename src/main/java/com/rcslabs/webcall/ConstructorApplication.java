package com.rcslabs.webcall;

import com.rcslabs.calls.ICallContext;
import com.rcslabs.messaging.IMessage;
import com.rcslabs.messaging.IMessageBroker;
import com.rcslabs.messaging.Message;
import com.rcslabs.rcl.core.IRclFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConstructorApplication extends BaseApplication {

    private final static Logger log = LoggerFactory.getLogger(ConstructorApplication.class);

    private boolean _ready;

    private ScheduledExecutorService scheduler;

    private final String SIP_USERNAME = "registerPhoneNumber";
    private final String SIP_PASSWORD = "registerPassword";
    private final String SIP_VIDEO_PHONE_NUMBER = "actualVideoPhoneNumber";
    private final String SIP_VOICE_PHONE_NUMBER = "actualAudioPhoneNumber";
    private final String VIDEO_CALL_DURATION = "durationVideo";
    private final String VOICE_CALL_DURATION = "durationAudio";
    private final String DURATION_NOTIFICATION_TIME = "durationNotificationTime";
    private final String DURATION_NOTIFICATION_DTMF = "durationNotificationDtmf";
    private final String DURATION_NOTIFICATION_SIPMSG = "durationNotificationSipmessage";

    public ConstructorApplication(String channelName, IConfig config, IMessageBroker broker, IRclFactory factory)
    {
        super(channelName, config, broker, factory);

        try {
            Class.forName("org.postgresql.Driver");
            scheduler = Executors.newScheduledThreadPool(1);  // TODO: should increase threads num in future???
            _ready = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected Connection dbConn() throws SQLException{
        return DriverManager.getConnection(
                config.getDatabaseUrl(), config.getDatabaseUser(), config.getDatabasePassword()
        );
    }

    @Override
    public boolean ready(){
        return _ready;
    }

    @Override
    public void beforeStartSession(IMessage message) throws Exception
    {
        String projectId = (String)message.get(IMessage.PROP_PROJECT_ID);
        Map<String, String> props = resolveButtonProperties(projectId);
        message.set(IMessage.PROP_USERNAME, props.get(SIP_USERNAME));
        message.set(IMessage.PROP_PASSWORD, props.get(SIP_PASSWORD));
    }

    @Override
    public ICallContext createCallContext(IMessage message)
    {
        ICallContext ctx;
        try {
            String projectId = (String)message.get(IMessage.PROP_PROJECT_ID);
            Map<String, String> props = resolveButtonProperties(projectId);
            List<Object> vv = (List<Object>) message.get(IMessage.PROP_VV);
            String bUri = props.get((Boolean) vv.get(1) ? SIP_VIDEO_PHONE_NUMBER : SIP_VOICE_PHONE_NUMBER);
            if(-1 == bUri.indexOf('@')){
                bUri += "@"+config.getSipServerHost()+":"+config.getSipServerPort();
            }
            message.set(IMessage.PROP_B_URI, bUri);

            ctx = super.createCallContext(message);

            ctx.set(IMessage.PROP_PROJECT_ID, projectId);
            // copy all interest parameters from database into call context
            if(props.containsKey(VIDEO_CALL_DURATION))
                ctx.set(VIDEO_CALL_DURATION, props.get(VIDEO_CALL_DURATION));
            if(props.containsKey(VOICE_CALL_DURATION))
                ctx.set(VOICE_CALL_DURATION, props.get(VOICE_CALL_DURATION));
            if(props.containsKey(DURATION_NOTIFICATION_TIME))
                ctx.set(DURATION_NOTIFICATION_TIME, props.get(DURATION_NOTIFICATION_TIME));
            if(props.containsKey(DURATION_NOTIFICATION_DTMF))
                ctx.set(DURATION_NOTIFICATION_DTMF, props.get(DURATION_NOTIFICATION_DTMF));
            if(props.containsKey(DURATION_NOTIFICATION_SIPMSG))
                ctx.set(DURATION_NOTIFICATION_SIPMSG, props.get(DURATION_NOTIFICATION_SIPMSG));
            return ctx;
        }catch (Exception e){
            log.error("Critical error on create call context for callId="+message.get(IMessage.PROP_CALL_ID), e);
        }
        return null;
    }

    @Override
    public void onCallStarted(ICallContext ctx, IMessage message)
    {
        super.onCallStarted(ctx, message);

        int callDuration = 0;
        if(ctx.hasVideo() && ctx.has(VIDEO_CALL_DURATION)){
            callDuration = Integer.valueOf(""+ctx.get(VIDEO_CALL_DURATION));
        }else if(ctx.has(VOICE_CALL_DURATION)){
            callDuration = Integer.valueOf(""+ctx.get(VOICE_CALL_DURATION));
        }

        if(callDuration > 0){
            scheduleCallTimerTask(ctx, callDuration);
            if(ctx.has(DURATION_NOTIFICATION_TIME)){
                int timeBeforeFinish = Integer.valueOf(""+ctx.get(DURATION_NOTIFICATION_TIME));
                if(timeBeforeFinish > 0 && timeBeforeFinish < callDuration){
                    scheduleNotificationTimerTask(ctx, callDuration-timeBeforeFinish, timeBeforeFinish);
                }
            }
        }
    }

    private Map<String, String> resolveButtonProperties(String buttonId) throws Exception
    {
        PreparedStatement pst = dbConn().prepareStatement(
                "SELECT a.*, ap.name AS key, ap.value AS value FROM application a " +
                        "LEFT JOIN applicationparameter ap ON (ap.application_id=a.id) WHERE a.name=?"
        );

        pst.setString(1, buttonId);
        ResultSet rs = pst.executeQuery();
        String k, v;
        Map<String, String> map = new HashMap<String, String>();

        while (rs.next())
        {
            k = rs.getString("key");
            v = rs.getString("value");

            if(k.equals(SIP_USERNAME) || k.equals(SIP_PASSWORD)
            || k.equals(SIP_VIDEO_PHONE_NUMBER) || k.equals(SIP_VOICE_PHONE_NUMBER)) {
                map.put(k, v);

            // Duration notification feature
            }else if(k.equals(VIDEO_CALL_DURATION) || k.equals(VOICE_CALL_DURATION)
            || k.equals(DURATION_NOTIFICATION_TIME) || k.equals(DURATION_NOTIFICATION_DTMF)
            || k.equals(DURATION_NOTIFICATION_SIPMSG)){
                if(!v.equals("0") && StringUtils.isNotBlank(v)){
                    map.put(k, StringUtils.trim(v));
                }
            }
        }

        rs.close();

        if(!(map.containsKey(SIP_USERNAME) && map.containsKey(SIP_PASSWORD))){
            throw new Exception("Auth parameters for button " + buttonId + " not found");
        }

        if(!(map.containsKey(SIP_VIDEO_PHONE_NUMBER) || map.containsKey(SIP_VOICE_PHONE_NUMBER))){
            throw new Exception("Call parameters for button " + buttonId + " not found");
        }

        return map;
    }

    class FinishCallTimerTask implements Runnable {

        private final ICallContext ctx;

        public FinishCallTimerTask(ICallContext ctx){
            this.ctx = ctx;
        }

        @Override
        public void run() {
            log.info("Finishing call {} by timer", ctx.getSipId());
            IMessage message = new Message(MessageType.HANGUP_CALL);
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            broker.publish(channelName, message);
        }
    }

    class FinishCallNotificationTimerTask implements Runnable {

        private final long timeBeforeFinish;
        private final ICallContext ctx;

        public FinishCallNotificationTimerTask(ICallContext ctx, long timeBeforeFinish) {
            this.ctx = ctx;
            this.timeBeforeFinish = timeBeforeFinish;
        }

        @Override
        public void run() {
            log.info("Notification call {} by timer", ctx.getSipId());
            IMessage message = new Message(MessageType.CALL_FINISH_NOTIFICATION);
            message.set(IMessage.PROP_SESSION_ID, ctx.getSessionId());
            message.set(IMessage.PROP_CALL_ID, ctx.getCallId());
            message.set(IMessage.PROP_TIME_BEFORE_FINISH, timeBeforeFinish);
            broker.publish(message.getClientChannel(), message);

        }
    }

    //schedules a call duration notification, if configured
    private void scheduleCallTimerTask(ICallContext ctx, long time) {
        log.info("Scheduling call {} finish after {} seconds", ctx.getSipId(), time);
        scheduler.schedule(
                new FinishCallTimerTask(ctx),
                time,
                TimeUnit.SECONDS
        );
    }

    private void scheduleNotificationTimerTask(ICallContext ctx, long time, long timeBeforeFinish) {
        log.info("Scheduling call {} finish notification after {} seconds", ctx.getSipId(), time);
        scheduler.schedule(
                new FinishCallNotificationTimerTask(ctx, timeBeforeFinish),
                time,
                TimeUnit.SECONDS
        );
    }
}
