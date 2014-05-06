package com.rcslabs.chat;

import com.rcslabs.a3.IDataStorage;

import java.util.Collection;

/**
 * Created by sx on 06.05.14.
 */
public class ChatRoom {

    private final IDataStorage<ChatUser> users;
    private final String label;
    private final String roomId;

    public ChatRoom(IDataStorage<ChatUser> users, String label) {
        this(users, label, label);
    }

    public ChatRoom(IDataStorage<ChatUser> users, String label, String roomId) {
        this.users = users;
        this.label = label;
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getLabel() {
        return label;
    }

    public ChatUser getUser(String username){
        for(ChatUser u : users.getAll()){
            if(u.getUsername().equals(username)) return u;
        }
        return null;
    }

    public Collection<ChatUser> getAllUsers(){
        return users.getAll();
    }

    public void joinUser(String sessionId, String username) {
        ChatUser user = new ChatUser(sessionId, username);
        users.set(sessionId, user);
    }

    public void unjoinUser(String sessionId) {
        users.delete(sessionId);
    }
}
