package com.rcslabs.a3.messaging;

import com.rcslabs.a3.IDataStorage;
import com.rcslabs.redis.IMessage;

import java.util.Map;

public interface IAlenaMessage<T extends Enum> extends IMessage, IDataStorage<Object> {

    IAlenaMessage cloneWithAnyType(T type);
	
	IAlenaMessage cloneWithSameType();
	
    T getType();

    String getTypz();

    Map<String, Object> getData();

	void set(String key, Object value);
	
	Object get(String key);

    boolean has(String key);

	void delete(String key);
	
	String getClientChannel();
}
