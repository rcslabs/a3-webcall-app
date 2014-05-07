package com.rcslabs.chat;

/**
 * Created by sx on 06.05.14.
 */
public class ChatUser {

    private final String sessionId;
    private final String username;    // this is not the same session username

    public ChatUser(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ChatUser)) return false;
        return ((ChatUser) obj).getSessionId().equals(sessionId)
            && ((ChatUser) obj).getUsername().equals(username);
    }
}
