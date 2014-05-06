package com.rcslabs.chat;

import com.rcslabs.a3.messaging.AbstractMessage;

/**
 * Created by sx on 22.04.14.
 */
public class ChatMessage extends AbstractMessage<ChatMessage.Type> {

    public static enum Type {
        JOIN_CHATROOM,
        JOIN_CHATROOM_OK,
        JOIN_CHATROOM_FAILED,
        UNJOIN_CHATROOM,
        CHAT_PRESENCE,
        CHAT_MESSAGE,
        CHAT_MESSAGE_SENT,
        CHAT_MESSAGE_FAILED,
        INCOMING_MESSAGE
    }

    public ChatMessage(Type type){
        super(type);
    }
}
