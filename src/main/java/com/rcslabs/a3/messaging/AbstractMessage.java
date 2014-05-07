package com.rcslabs.a3.messaging;

import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMessage<T extends Enum> implements IMessage<T> {

    protected final static Logger log = LoggerFactory.getLogger(AbstractMessage.class);

    // Messages`s class short name (semantic is a type+clazz)
    private String typz;

    // Message`s concrete type such as START_CALL
	private T type;

	private Map<String, Object> data;
	
	public Map<String, Object> getData() {
		return data;
	}

	public AbstractMessage(T type) {
		super();
		this.type = type; 
		this.data = new HashMap<String, Object>();
	}

	@Override
	public String getClientChannel(){
		return (null != get(MessageProperty.SESSION_ID) ? "sid:"+get(MessageProperty.SESSION_ID) : null);
	}
	
	@Override	
	public T getType(){
		return type;
	}

    public String getTypz(){
        if(typz == null){
            String[] t = getClass().getName().split("\\.");
            typz = t[t.length-1];
        }
        return typz;
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
    public Collection<Object> getAll() {
        return data.values();
    }

    @Override
	public String toString() {
		String s = "["+getTypz()+"."+ type+" ";
		for(String key : data.keySet()){
			if(MessageProperty.SDP.equals(key)){ s += "sdp=..., "; continue; }
            if(MessageProperty.CONTENT.equals(key)){ s += "content=..., "; continue; }
			if("type".equals(key)){ continue; }
			if("password".equals(key)){ continue; }
			s += (key + "=" + data.get(key)+", ");
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
