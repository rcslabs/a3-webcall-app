package com.rcslabs.a3.messaging;

import com.rcslabs.a3.IDataStorage;

import java.util.Map;

public interface IMessage<T extends Enum> extends IDataStorage<Object> {

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
