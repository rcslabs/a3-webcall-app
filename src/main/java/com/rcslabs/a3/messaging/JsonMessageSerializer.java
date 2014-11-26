package com.rcslabs.a3.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rcslabs.redis.IMessage;
import com.rcslabs.redis.IMessageSerializer;
import com.rcslabs.redis.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.util.SafeEncoder;

/**
 * Created by ykrkn on 06.11.14.
 */
public class JsonMessageSerializer implements IMessageSerializer {

    protected final static Logger log = LoggerFactory.getLogger(JsonMessageSerializer.class);

    private GsonBuilder builder;
    private Gson gson;
    private MessageTypeAdapter<IAlenaMessage> adapter;

    public JsonMessageSerializer(){
        builder = new GsonBuilder();
        adapter = new MessageTypeAdapter<IAlenaMessage>();
    }

    /**
     * Registering message class must have the inner public enumerator named "Type"
     * @param clazz
     */
    public void registerMessageClass(Class<?> clazz){
        if(gson != null){
            throw new RuntimeException("You cannot add class to registry after serializer started.");
        }
        adapter.registerClass(clazz);
    }

    private void init(){
        builder.registerTypeHierarchyAdapter(IAlenaMessage.class, adapter);
        gson = builder.create();
    }

    @Override
    public byte[] serialize(IMessage obj) throws MessagingException {
        if(gson == null) init();
        return SafeEncoder.encode(gson.toJson(obj));
    }

    @Override
    public IMessage deserialize(byte[] bytes) throws MessagingException {
        if(gson == null) init();
        return gson.fromJson(SafeEncoder.encode(bytes), IAlenaMessage.class);
    }
}
