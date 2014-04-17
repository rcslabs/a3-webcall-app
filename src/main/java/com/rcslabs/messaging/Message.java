package com.rcslabs.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Message<T extends Enum> implements IMessage<T> {

    protected final static Logger log = LoggerFactory.getLogger(Message.class);

	private T type;
	
	private Map<String, Object> data;
	
	public Map<String, Object> getData() {
		return data;
	}

	public Message(T type) {
		super();
		this.type = type; 
		this.data = new HashMap<String, Object>();
	}

	@Override
	public String getClientChannel(){
		return (null != get(IMessage.PROP_SESSION_ID) ? "sid:"+get(IMessage.PROP_SESSION_ID) : null);
	}
	
	@Override	
	public T getType(){
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
			if(IMessage.PROP_SDP.equals(key)){ s += ", sdp=..."; continue; }
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

    private static final Map<String, Constructor<?>> reflectionCache = new ConcurrentHashMap<String, Constructor<?>>();

	public IMessage cloneWithAnyType(T type){
        try {
            String typeName = type.getClass().getName();
            Constructor<?> cnst = null;

            if(reflectionCache.containsKey(typeName)){
                cnst = reflectionCache.get(typeName);
            } else {
                String[] classNameAndType = typeName.split("\\$");
                Class<?> clazz = Class.forName(classNameAndType[0]);
                cnst = clazz.getConstructor(type.getClass());
                reflectionCache.put(typeName, cnst);
            }

            IMessage aMessage = (IMessage)cnst.newInstance(type);
            for(String key : data.keySet()){
                aMessage.set(key, data.get(key));
            }
            return aMessage;
        } catch (Exception e) {
            log.error("Error on message clone", e);
            return null;
        }
	}
}
