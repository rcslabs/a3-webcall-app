package com.rcslabs.messaging;

import java.util.Map;

public interface IMessage<T extends Enum> {

    static final String PROP_SESSION_ID	= "sessionId";
    static final String PROP_CLIENT_ID  = "clientId";
    static final String PROP_CALL_ID    = "callId";
    static final String PROP_ROOM_ID    = "roomId";
    static final String PROP_POINT_ID   = "pointId";
    static final String PROP_SERVICE    = "service";
    static final String PROP_SDP        = "sdp";
    static final String PROP_A_URI      = "aUri";
    static final String PROP_B_URI      = "bUri";
    static final String PROP_SENDER     = "sender";
    static final String PROP_PROFILE    = "profile";
    static final String PROP_REASON     = "reason";
    static final String PROP_STAGE      = "stage";
    static final String PROP_CC         = "cc";
    static final String PROP_VV         = "vv";
    static final String PROP_USERNAME   = "username";
    static final String PROP_PASSWORD   = "password";
    static final String PROP_PROJECT_ID = "projectId";
    static final String PROP_DTMF       = "dtmf";
    static final String PROP_TIME_BEFORE_FINISH = "timeBeforeFinish";

	IMessage cloneWithAnyType(T type);
	
	IMessage cloneWithSameType();
	
    T getType();

    Map<String, Object> getData();

	void set(String key, Object value);
	
	Object get(String key);

    boolean has(String key);

	void delete(String key);
	
	String getClientChannel();
}
