package com.rcslabs.a3.messaging;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sx on 17.04.14.
 */
public class MessageTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T>{

    protected final static Logger log = LoggerFactory.getLogger(MessageTypeAdapter.class);

    private final Map<String, Class> jsonTypeToClassMap  = new ConcurrentHashMap<String, Class>();
    private final Map<String, Enum>  jsonTypeToEnumTypeMap = new ConcurrentHashMap<String, Enum>();

    public void registerClass(Class<?> clazz)
    {
        Class[] classes = clazz.getDeclaredClasses();
        for(Class c : classes){
            if(c.getName().endsWith("Type")){
                Object[] enumCnst = c.getEnumConstants();
                for(Object o : enumCnst){
                    jsonTypeToEnumTypeMap.put(o.toString(), (Enum)o);
                    jsonTypeToClassMap.put(o.toString(), clazz);
                }
            }
        }
    }

    IMessage createEmptyMessage(String jsonType) throws Exception {
        Class<IMessage> clazz = jsonTypeToClassMap.get(jsonType);
        Enum messageType = jsonTypeToEnumTypeMap.get(jsonType);
        return clazz.getConstructor(messageType.getClass()).newInstance(messageType);
    }

    /** MessageDeserializer implementation */

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        try{
            return (T)handleObject(json.getAsJsonObject(), context);
        }catch(Exception e){
            log.error("Error on json deserialization", e);
            return null;
        }
    }

    /** MessageSerializer implementation */

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context)
    {
        try{
            JsonObject obj = new JsonObject();
            obj.addProperty("type", ((IMessage)src).getType().toString());
            Iterator it = ((IMessage)src).getData().entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Object> pairs = (Map.Entry)it.next();
                String key = pairs.getKey();
                Object val = pairs.getValue();

                if(null == val){
                    obj.add(key, null);
                }else if(val instanceof String){
                    obj.addProperty(key, (String) val);
                }else if(val instanceof Number){
                    obj.addProperty(key, (Number) val);
                }else if(val instanceof Boolean){
                    obj.addProperty(key, (Boolean) val);
                }else if(val instanceof Map<?, ?>){
                    obj.add(key, context.serialize(val, Map.class));
                }else if(val instanceof List<?>){
                    obj.add(key, context.serialize(val, List.class));
                    // TODO: serialize }else if(val instanceof ClientCapabilities){
                    //	obj.add(key, context.serialize(((ClientCapabilities) val).getRawData(), Map.class));
                }else{
                    throw new JsonSyntaxException("Unknown type " + val.getClass().getName() + " at key="+key);
                }
            }
            return obj;

        } catch(Exception e){
            log.error("Error on json serialization", e);
            return null;
        }
    }

    // FIXME: invalid deserialization int 777 to float like a 777.0
    private IMessage handleObject(JsonObject json, JsonDeserializationContext context) throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()){
            Object value = context.deserialize(entry.getValue(), Object.class);
            map.put(entry.getKey(), value);
        }

        IMessage m = createEmptyMessage((String) map.get("type"));
        for(String key : map.keySet()){
            if("type".equals(key)){ continue; }
            m.set(key, map.get(key));
        }
        return m;
    }
}
