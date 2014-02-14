package com.rcslabs.messaging;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rcslabs.webcall.MessageType;

class MessageDeserializer implements JsonDeserializer<IMessage>
{
	public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		return handleObject(json.getAsJsonObject(), context);
	}

	// FIXME: invalid deserialization int 777 to float like a 777.0
	private Message handleObject(JsonObject json, JsonDeserializationContext context)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (Map.Entry<String, JsonElement> entry : json.entrySet()){ 				
			Object value = context.deserialize(entry.getValue(), Object.class);
			map.put(entry.getKey(), value);
		}
		
		Message m = new Message(MessageType.valueOf((String)map.get("type")));
		for(String key : map.keySet()){
			if("type".equals(key)){ continue; }
			m.set(key, map.get(key));
		}
		
		return m;
	}
}