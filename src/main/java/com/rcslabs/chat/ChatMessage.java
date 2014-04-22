package com.rcslabs.chat;

import com.rcslabs.a3.messaging.AbstractMessage;

/**
 * Created by sx on 22.04.14.
 */
public class ChatMessage extends AbstractMessage<ChatMessage.Type> {

    public static enum Type {
        JOIN_CHATROOM,
        UNJOIN_CHATROOM,
        TEXT_MESSAGE
    }

    public ChatMessage(Type type){
        super(type);
    }
}
