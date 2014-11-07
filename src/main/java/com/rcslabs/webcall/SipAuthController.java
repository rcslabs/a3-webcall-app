package com.rcslabs.webcall;

import com.rcslabs.a3.InMemoryDataStorage;
import com.rcslabs.a3.auth.*;
import com.rcslabs.a3.messaging.AuthMessage;
import com.rcslabs.a3.messaging.IAlenaMessage;
import com.rcslabs.a3.messaging.MessageProperty;
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
import com.rcslabs.a3.messaging.CallMessage;
import com.ykrkn.redis.RedisConnector;

public class SipAuthController extends AbstractAuthController
        implements IConnectionListener, ITelephonyServiceListener {

	protected ICallAppConfig config;
    protected IRclFactory factory;

	public SipAuthController(RedisConnector redisConnector, String name, ICallAppConfig config, IRclFactory factory) {
		super(name, redisConnector, new InMemoryDataStorage<ISession>());
        this.config = config;
        this.factory = factory;
	}

    /**
	 * IAuthService implementation 
	 */

    @Override
    public void onAuthMessage(AuthMessage message) {
        if(AuthMessage.Type.START_SESSION == message.getType()){
            startSession(new Session(message));

        }else if(AuthMessage.Type.CLOSE_SESSION == message.getType()){
            String p0 = (String) message.get(MessageProperty.SESSION_ID);
            ISession session = findSession(p0);
            if(null == session){
                log.warn("Session not found for id=" + p0);
            }else{
                closeSession(p0);
            }
        }
    }

	public void startSession(ISession session) 
	{
		try{
			log.info("Start session " + session);
			IConnection conn = factory.newConnection();
			conn.addListener(this);
		
			ConnectionParams connParams = new ConnectionParams();
			connParams.setPhoneNumber(session.getUsername());
			connParams.setPassword(session.getPassword());
			connParams.setPresenceEnabled(false);		

			((Session)session).setSessionId(conn.getId());
			storage.set(conn.getId(), session);
			
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
            IAlenaMessage message = new CallMessage(CallMessage.Type.INCOMING_CALL);
            message.set(MessageProperty.SERVICE, session.getService());
            message.set(MessageProperty.SESSION_ID, sessionId);
            message.set(MessageProperty.CALL_ID, call.getId());
            message.set(MessageProperty.A_URI, prepareCallUri(((ICallParams)call).getFrom()));
            message.set(MessageProperty.B_URI, prepareCallUri(((ICallParams)call).getTo()));
            message.set(MessageProperty.SDP, ((JainSipCall)call).getSdpObject().getOfferer());
            redisConnector.publish(session.getService(), message);
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
}
