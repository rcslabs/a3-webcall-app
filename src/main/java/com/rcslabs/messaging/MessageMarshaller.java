package com.rcslabs.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Created by sx on 15.04.14.
 */
public class MessageMarshaller {

    protected final static Logger log = LoggerFactory.getLogger(MessageMarshaller.class);

    private GsonBuilder builder;
    private Gson gson;
    private MessageTypeAdapter<IMessage> adapter;

    private static MessageMarshaller instance;

    private MessageMarshaller(){
        builder = new GsonBuilder();
        adapter = new MessageTypeAdapter<IMessage>();
    }

    public static synchronized MessageMarshaller getInstance(){
        if(instance == null){
            instance = new MessageMarshaller();
        }
        return instance;
    }



    public void start(){
        builder.registerTypeHierarchyAdapter(IMessage.class, adapter);
        gson = builder.create();
    }

    /**
     * Registering message class must have the inner public enumerator named "Type"
     * @param clazz
     */
    public void registerMessageClass(Class<?> clazz){
        if(gson != null){
            throw new RuntimeException("You cannot add class to registry after marshaller started.");
        }
        adapter.registerClass(clazz);
    }

    public IMessage fromJson(String value){
        return gson.fromJson(value, IMessage.class);
    }

    public String toJson(IMessage value){
        return gson.toJson(value);
    }
}
