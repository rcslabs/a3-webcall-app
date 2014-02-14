package com.rcslabs.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rcslabs.webcall.MessageType;

import java.util.HashMap;
import java.util.Map;

public class Message implements IMessage {
	
	private static Gson gson; 
	private boolean canceled;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Message.class, new MessageDeserializer());
		gsonBuilder.registerTypeAdapter(Message.class, new MessageSerializer());
		gson = gsonBuilder.create();		
	}
	
	private String service;
	private MessageType type;
	
	private Map<String, Object> data;
	
	public Map<String, Object> getData() {
		return data;
	}

    public Message(String service, MessageType type) {
        this(type);
        this.service = service;
    }

	public Message(MessageType type) {
		super();
		this.canceled = false;
		this.type = type; 
		this.data = new HashMap<String, Object>();
	}
	
	@Override
	public String getClientChannel(){
		return (null != get(IMessage.PROP_SESSION_ID) ? "sid:"+get(IMessage.PROP_SESSION_ID) : null);
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
	
	public static IMessage fromJson(String value){
		return gson.fromJson(value, Message.class);
	}
	
	public static String toJson(IMessage value){
		return gson.toJson(value);
	}

	@Override
	public String toString() {
		String s = "Message [type=" + type;
		for(String key : data.keySet()){
			if(IMessage.PROP_SDP.equals(key)){ s += ", sdp=SKIPPED"; continue; }
			if("type".equals(key)){ continue; }
			if("password".equals(key)){ continue; }
			s += (", " + key + "=" + data.get(key));
		}		
		s += "]";
		return s; 
	}

	public IMessage cloneWithSameType(){
		return cloneWithAnyType(type);
	}
	
	public IMessage cloneWithAnyType(MessageType type){
		IMessage aMessage = new Message(service, type);
		for(String key : data.keySet()){
			aMessage.set(key, data.get(key));
		}
		return aMessage;
	}

	@Override
	public String getService() {
		return service;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel() {
		canceled = true;
	}
}
