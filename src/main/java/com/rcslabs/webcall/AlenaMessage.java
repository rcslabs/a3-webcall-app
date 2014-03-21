package com.rcslabs.webcall;

import com.rcslabs.redis.IMessage;
import com.rcslabs.redis.ITypedMessage;

import java.util.HashMap;
import java.util.Map;

public class AlenaMessage implements IMessage, ITypedMessage<MessageType> {

    public static final String PROP_SESSION_ID	= "sessionId";
    public static final String PROP_CLIENT_ID  = "clientId";
    public static final String PROP_CALL_ID    = "callId";
    public static final String PROP_ROOM_ID    = "roomId";
    public static final String PROP_POINT_ID   = "pointId";
    public static final String PROP_SERVICE    = "service";
    public static final String PROP_SDP        = "sdp";
    public static final String PROP_A_URI      = "aUri";
    public static final String PROP_B_URI      = "bUri";
    public static final String PROP_SENDER     = "sender";
    public static final String PROP_PROFILE    = "profile";
    public static final String PROP_REASON     = "reason";
    public static final String PROP_STAGE      = "stage";
    public static final String PROP_CC         = "cc";
    public static final String PROP_VV         = "vv";
    public static final String PROP_USERNAME   = "username";
    public static final String PROP_PASSWORD   = "password";
    public static final String PROP_PROJECT_ID = "projectId";
    public static final String PROP_DTMF       = "dtmf";
    public static final String PROP_TIME_BEFORE_FINISH = "timeBeforeFinish";

    private boolean canceled;

	private String service;
	private MessageType type;
	
	private Map<String, Object> data;
	
	public Map<String, Object> getData() {
		return data;
	}

    public AlenaMessage(String service, MessageType type) {
        this(type);
        this.service = service;
    }

	public AlenaMessage(MessageType type) {
		super();
		this.canceled = false;
		this.type = type; 
		this.data = new HashMap<String, Object>();
	}

	public String getClientChannel(){
		return (null != get(PROP_SESSION_ID) ? "sid:"+get(PROP_SESSION_ID) : null);
	}
	
	@Override	
	public MessageType getType(){
		return type;
	}
	
	@Override	
	public void set(String key, Object value){
		data.put(key, value);
	}
	
	@Override	
	public Object get(String key){ 
		return data.get(key); 
	}

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
	public void delete(String key) {
		data.remove(key);
	}

	@Override
	public String toString() {
		String s = "Message [type=" + type;
		for(String key : data.keySet()){
			if(PROP_SDP.equals(key)){ s += ", sdp=SKIPPED"; continue; }
			if("type".equals(key)){ continue; }
			if("password".equals(key)){ continue; }
			s += (", " + key + "=" + data.get(key));
		}		
		s += "]";
		return s; 
	}

    @Override
	public IMessage cloneWithSameType(){
		return cloneWithAnyType(type);
	}

    @Override
	public IMessage cloneWithAnyType(MessageType type){
        IMessage aMessage = new AlenaMessage(service, type);
		for(String key : data.keySet()){
			aMessage.set(key, data.get(key));
		}
		return aMessage;
	}

	public String getService() {
		return service;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void cancel() {
		canceled = true;
	}
}
