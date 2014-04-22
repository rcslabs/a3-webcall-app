package com.rcslabs.webcall.auth;

import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.a3.auth.IAuthControllerDelegate;
import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.auth.ISessionStorage;
import com.rcslabs.webcall.ICallAppConfig;
import com.rcslabs.webcall.calls.CallMessage;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBroker;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.rcl.core.entity.ConnectionParams;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;
import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SipAuthController
        implements IAuthController, IAuthControllerDelegate,
	IConnectionListener, IMessageBrokerDelegate, ITelephonyServiceListener {
	
	protected final static Logger log = LoggerFactory.getLogger(SipAuthController.class);

    protected ISessionStorage storage;
	protected int ttl;
	protected ICallAppConfig config;
    protected IMessageBroker broker;
    protected IRclFactory factory;

	public SipAuthController(ICallAppConfig config, IMessageBroker broker, IRclFactory factory) {
		super();
        this.config = config;
        this.broker = broker;
        this.factory = factory;
        setTimeToLive( config.getSipExpires() );
	}

    @Override
    public void setSessionStorage(ISessionStorage storage) {
        this.storage = storage;
    }

	/** 
	 * IMessageBrokerDelegate implementation 
	 */
	
	public void onMessageReceived(String channel, IMessage message)
	{
        try{
            if(AuthMessage.Type.START_SESSION == message.getType()){
                String p0 = (String) message.get(MessageProperty.SERVICE);
                String p1 = (String) message.get(MessageProperty.USERNAME);
                String p2 = (String) message.get(MessageProperty.PASSWORD);
                String p3 = (String) message.get(MessageProperty.CLIENT_ID);
                String p4 = (String) message.get(MessageProperty.SENDER);
                Session session = new Session(p0, p1, p2);
                session.setClientId(p3);
                session.setSender(p4);
                startSession(session);

            }else if(AuthMessage.Type.CLOSE_SESSION == message.getType()){
                String p0 = (String) message.get(MessageProperty.SESSION_ID);
                ISession session = findSession(p0);
                if(null == session){
                    log.warn("Session not found for id=" + p0);
                }else{
                    closeSession(p0);
                }
            }
        } catch(Exception e){
            handleOnMessageException(message, e);
        }
	}

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {
        log.error(e.getMessage(), e);
    }

    /**
	 * IAuthService implementation 
	 */
	
	public void startSession(ISession session) 
	{
		try{
			log.info("Start session " + session);
			IConnection conn = factory.newConnection();
			conn.addListener(this);
		
			ConnectionParams connParams = new ConnectionParams();
			connParams.setPhoneNumber(session.getUsername());
			//connParams.setUserName(session.getUsername());
			connParams.setPassword(session.getPassword());
			connParams.setPresenceEnabled(false);		

			((Session)session).setSessionId(conn.getId());
			storage.set(conn.getId(), ((Session) session));
			
			conn.open(connParams);			
		}catch(Exception e){
			log.error(e.getMessage());
			onSessionFailed(session, "FAILED");
		} 
	}

	public void closeSession(String sessionId) {
		log.info("Close session id=" + sessionId);
		ISession session = findSession(sessionId);

		if(null == session){ 
			log.warn("Session not found id=" + sessionId);
			return; 
		}
		
		IConnection conn = factory.findConnection(sessionId);
		
		if(null == conn){ 
			log.warn("IConnection not found id=" + sessionId);
			return; 
		}
		
		conn.close();
		storage.delete(sessionId);
	}
	
	public ISession findSession(String value) 
	{
		if(!storage.has(value)) return null;
		return storage.get(value);
	}

    public void setTimeToLive(int value) {
		ttl = value;	
	}

	public int getTimeToLive() {
		return ttl;
	}
	
	/**
	 * IConnectionListener implementation 
	 */
	
	public void onConnecting(IConnectionEvent event) {
        ISession session = findSession(event.getConnection().getId());

        if(null == session){
            log.error("Session not found for id=" + event.getConnection().getId());
        }else{
            session.onEvent(new SessionSignal(AuthMessage.Type.START_SESSION));
        }
    }

	public void onConnected(IConnectionEvent event) 
	{ 
		ISession session = findSession(event.getConnection().getId());

		if(null == session){ 
			log.error("Session not found for id=" + event.getConnection().getId());
		}else{
			try {
				// find session with the same username and kick him
                ISession exist = storage.findPreviousSession(session);
                if(exist != null) {
                    onSessionFailed(exist, "REPLACED");
                    log.warn("Session " + exist.getSessionId()
                            + " replaced with session " + session.getSessionId());
                    IConnection conn = factory.findConnection(exist.getSessionId());
                    if(null != conn){
                        try {
                            conn.getService(ITelephonyService.class).removeListener(this);
                        } catch (ServiceNotEnabledException e) { /* no critical error here */}
                    }
                }
				event.getConnection().getService(ITelephonyService.class).addListener(this);
			} catch (ServiceNotEnabledException e) {
				log.error(e.getMessage());
				log.debug("{}", e.getStackTrace());
			}
			
			String uri = event.getConnection().getUri().toString();
			((Session) session).setUri(uri);
			onSessionStarted(session);
		}
	}

	public void onConnectionBroken(IConnectionEvent event) 
	{
		ISession session = findSession(event.getConnection().getId());

		if(null == session){ 
			log.error("Session not found for username=" + event.getConnection().getId());
		}else{
			try {
				event.getConnection().getService(ITelephonyService.class).removeListener(this);
			} catch (ServiceNotEnabledException e) {
				log.error(e.getMessage());
				log.debug("{}", e.getStackTrace());
			}	

			onSessionClosed(session);
            storage.delete(session.getSessionId());
		}		
	}

	public void onConnectionFailed(IConnectionEvent event) 
	{
		ISession session = findSession(event.getConnection().getId());

		if(null == session){ 
			log.error("Session not found for username=" + event.getConnection().getId());
		}else{
			try {
				event.getConnection().getService(ITelephonyService.class).removeListener(this);
			} catch (ServiceNotEnabledException e) {
				log.error(e.getMessage());
				log.debug("{}", e.getStackTrace());
			}	
			
			String reason = "FAILED";
			if(null != event.getErrorInfo() && null != event.getErrorInfo().getErrorText()){
				reason = event.getErrorInfo().getErrorText();
			}
			onSessionFailed(session, reason);
            storage.delete(session.getSessionId());
		}
	}

	public void onConnectionError(IConnectionEvent event)
	{
		ISession session = findSession(event.getConnection().getId());

		if(null == session){ 
			log.error("Session not found for username=" + event.getConnection().getId());
			return; 
		}else{
			try {
				event.getConnection().getService(ITelephonyService.class).removeListener(this);
			} catch (ServiceNotEnabledException e) {
				log.error(e.getMessage());
				log.debug("{}", e.getStackTrace());
			}
			
			String reason = "FAILED";
			if(null != event.getErrorInfo() && null != event.getErrorInfo().getErrorText()){
				reason = event.getErrorInfo().getErrorText();
			}
			onSessionFailed(session, reason);
			storage.delete(session.getSessionId());
		}
	}

	/**
	 * ITelephonyServiceListener implementation
	 */
	
	public void onIncomingCall(ICall call, ITelephonyEvent event) {
        String sessionId = event.getConnection().getId();
        ISession session = findSession(sessionId);
        if(null == session){
            log.error("Session " + sessionId + " not found");
        } else {
            IMessage message = new CallMessage(CallMessage.Type.INCOMING_CALL);
            message.set(MessageProperty.SERVICE, session.getService());
            message.set(MessageProperty.SESSION_ID, sessionId);
            message.set(MessageProperty.CALL_ID, call.getId());
            message.set(MessageProperty.A_URI, prepareCallUri(((ICallParams)call).getFrom()));
            message.set(MessageProperty.B_URI, prepareCallUri(((ICallParams)call).getTo()));
            message.set(MessageProperty.SDP, ((JainSipCall)call).getSdpObject().getOfferer());
            broker.publish(session.getService(), message);
        }
	}

	public void onCancel(ICall call, ITelephonyEvent event) {
		log.info(event.toString());
	}

    private String prepareCallUri(String uri){
        int b = uri.indexOf('<')+1;
        int e = uri.indexOf('>');
        return uri.substring(b, e);
    }

	/**
	 * IAuthServiceDelegate implementation 
	 */
		
	public void onSessionStarted(ISession session) {
		log.info("onSessionStarted " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_STARTED));
		IMessage message = new AuthMessage(AuthMessage.Type.SESSION_STARTED);
        message.set(MessageProperty.SERVICE, session.getService());
		message.set(MessageProperty.SESSION_ID, session.getSessionId());
		message.set(MessageProperty.CLIENT_ID, session.getClientId());
		broker.publish(session.getSender(), message);
	}

	public void onSessionFailed(ISession session, String reason) {
		log.info("onSessionFailed " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_FAILED));
		IMessage message = new AuthMessage(AuthMessage.Type.SESSION_FAILED);
        message.set(MessageProperty.SERVICE, session.getService());
		message.set(MessageProperty.SESSION_ID, session.getSessionId());
		message.set(MessageProperty.CLIENT_ID, session.getClientId());
		message.set(MessageProperty.REASON, reason);
		broker.publish(session.getSender(), message);
	}

	public void onSessionClosed(ISession session) {
		log.info("onSessionClosed " + session);
        session.onEvent(new SessionSignal(AuthMessage.Type.SESSION_CLOSED));
		IMessage message = new AuthMessage(AuthMessage.Type.SESSION_CLOSED);
        message.set(MessageProperty.SERVICE, session.getService());
		message.set(MessageProperty.SESSION_ID, session.getSessionId());
		message.set(MessageProperty.CLIENT_ID, session.getClientId());
		broker.publish(session.getSender(), message);
	}
}
