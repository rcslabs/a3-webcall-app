package com.rcslabs.chat;

import com.rcslabs.a3.IDataStorage;
import com.rcslabs.a3.InMemoryDataStorage;
import com.rcslabs.a3.auth.*;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.RedisConnector;
import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 22.04.14.
 */
public class BaseChatApplication implements IChatApplication {

    protected final static Logger log = LoggerFactory.getLogger(BaseChatApplication.class);

    protected final String messagingChannel;
    protected final RedisConnector redisConnector;
    protected final IAuthController authController;
    protected final IDataStorage<ChatRoom> rooms;
    protected final IDataStorage<ChatMessage> messages;

    private final String ENTER_ROOM = "ENTER_ROOM";
    private final String LEAVE_ROOM = "LEAVE_ROOM";

    public BaseChatApplication(String messagingChannel, RedisConnector redisConnector) {
        this.messagingChannel = messagingChannel;
        this.redisConnector = redisConnector;
        this.rooms = new InMemoryDataStorage<>();
        this.messages = new InMemoryDataStorage<>();
        this.authController = new ChatAuthController(redisConnector, new InMemorySessionStorage(), 3600);
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public IAuthController getAuthController() {
        return authController;
    }

    @Override
    public IConfig getConfig() {
        return null;
    }

    @Override
    public String getMessagingChannel() {
        return messagingChannel;
    }

    @Override
    public void beforeStartSession(IMessage message) throws Exception {}

    @Override
    public void onMessageReceived(String channel, IMessage message)
    {
        try {
            validateMessage(message);
            if(message.getType() instanceof AuthMessage.Type)
                handleAuthMessage((AuthMessage) message);
            else if(message.getType() instanceof ChatMessage.Type)
                handleChatMessage((ChatMessage) message);
            else
                log.warn("Unhandled message " + message.getType());
        } catch (Exception e) {
            handleOnMessageException(message, e);
        }
    }

    @Override
    public void validateMessage(IMessage message) throws InvalidMessageException
    {
        if(message.getType() == AuthMessage.Type.START_SESSION){ return; }

        // all messages except START_SESSION must contains parameter "sessionId"
        // validate it and throws an Exception unsuccessfully

        if(!message.has(MessageProperty.SESSION_ID)){
            throw new InvalidMessageException("Skip the message without sessionId " + message);
        } else {
            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            ISession session = authController.findSession(sessionId);
            if(null == session){
                log.warn("Session for message not found " + message);
            }
        }
    }

    @Override
    public void handleOnMessageException(IMessage message, Throwable e) {
        log.error(e.getMessage(), e);
        if(message.getType() == AuthMessage.Type.START_SESSION){
            authController.onSessionFailed(new CriticalFailedSession(message), "Critical error");
        }
    }

    protected void handleAuthMessage(AuthMessage message) throws Exception{
        switch (message.getType())
        {
            case START_SESSION:
                authController.startSession(new Session(message));
                break;

            case CLOSE_SESSION:
                String sessionId = (String) message.get(MessageProperty.SESSION_ID);
                closeSessionInternal(sessionId);
                authController.closeSession(sessionId);
                break;

            default:
                log.warn("Unhandled message " + message.getType());
        }
    }

    protected void handleChatMessage(ChatMessage message) throws Exception {
        switch (message.getType())
        {
            case JOIN_CHATROOM:
                onJoinChatroomMessage(message);
                break;

            case UNJOIN_CHATROOM:
                onUnjoinChatroomMessage(message);
                break;

            case CHAT_MESSAGE:
                onChatMessage(message);
                break;

            default:
                log.warn("Unhandled message " + message.getType());
        }
    }

    protected void onJoinChatroomMessage(ChatMessage message){
        ChatMessage response = null;
        ChatRoom room = null;
        try{
            String username = (String)message.get(MessageProperty.USERNAME);
            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            String roomId = (String)message.get(MessageProperty.ROOM_ID);

            if(!rooms.has(roomId)){  // if has no room - simply create it
                room = new ChatRoom(new InMemoryDataStorage<ChatUser>(), roomId);
                rooms.set(roomId, room);
            }else{
                room = rooms.get(roomId);
            }

            if(null != room.getUser(username)){
                throw new RuntimeException("User with same name already exists in room");
            }

            room.joinUser(sessionId, username);
            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.JOIN_CHATROOM_OK);
            redisConnector.publish(message.getClientChannel(), response);

            // send message to all users in the room
            ChatMessage message2 = new ChatMessage(ChatMessage.Type.CHAT_PRESENCE);
            message2.set(MessageProperty.USERNAME, username);
            message2.set(MessageProperty.STAGE, ENTER_ROOM);
            sendMessageForRoom(message2, room);

            // send 'enter' from all users already exist in the room to joined user
            for(ChatUser u : room.getAllUsers()){
                ChatMessage message3 = new ChatMessage(ChatMessage.Type.CHAT_PRESENCE);
                message3.set(MessageProperty.USERNAME, u.getUsername());
                message3.set(MessageProperty.STAGE, ENTER_ROOM);
                message3.set(MessageProperty.SESSION_ID, sessionId);
                message3.set(MessageProperty.ROOM_ID, room.getRoomId());
                redisConnector.publish(message.getClientChannel(), message3);
            }

        } catch (Exception e){
            log.error(e.getMessage(), e);
            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.JOIN_CHATROOM_FAILED);
            redisConnector.publish(message.getClientChannel(), response);
        }
    }

    protected void onUnjoinChatroomMessage(ChatMessage message){
        try{
            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            String roomId = (String)message.get(MessageProperty.ROOM_ID);
            if(!rooms.has(roomId)){
                throw new RuntimeException("Room "+roomId+" not found");
            }

            ChatRoom room = rooms.get(roomId);
            ChatUser user = room.getUser(sessionId);

            if(null != user){
                room.unjoinUser(sessionId);
                // send message to all users in the room
                ChatMessage message2 = new ChatMessage(ChatMessage.Type.CHAT_PRESENCE);
                message2.set(MessageProperty.USERNAME, user.getUsername());
                message2.set(MessageProperty.STAGE, LEAVE_ROOM);
                sendMessageForRoom(message2, room);
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    protected void onChatMessage(ChatMessage message) throws Exception{
        ChatMessage response = null;
        try{
            String messageId = (String)message.get(MessageProperty.MESSAGE_ID);
            String sessionId = (String)message.get(MessageProperty.SESSION_ID);
            String roomId = (String)message.get(MessageProperty.ROOM_ID);
            String sender = (String)message.get(MessageProperty.USERNAME);
            //String content = (String)message.get(MessageProperty.CONTENT);

            if(!rooms.has(roomId)){
                throw new RuntimeException("Room "+roomId+" not found");
            }

            ChatRoom room = rooms.get(roomId);
            ChatUser user = room.getUser(sender);

            if(null == user){
                throw new RuntimeException("Sender not in the room");
            }

            if(!user.getSessionId().equals(sessionId)){
                throw new RuntimeException("Sender session and user-in-the-room session are not equals!!!");
            }

            ChatMessage messageTo = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.INCOMING_MESSAGE);
            sendMessageForRoom(messageTo, room);

            messages.set(messageId, message); // TODO: linked set

            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.CHAT_MESSAGE_SENT);
        } catch (Exception e){
            log.error(e.getMessage(), e);
            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.CHAT_MESSAGE_FAILED);
        } finally {
            redisConnector.publish(message.getClientChannel(), response);
        }
    }

    protected void sendMessageForRoom(ChatMessage message, ChatRoom room){
        message.set(MessageProperty.ROOM_ID, room.getRoomId());
        for(ChatUser u : room.getAllUsers()){
            ChatMessage m = (ChatMessage)message.cloneWithSameType();
            m.set(MessageProperty.SESSION_ID, u.getSessionId());
            redisConnector.publish(m.getClientChannel(), m);
        }
    }

    protected void closeSessionInternal(String sessionId)
    {
        for(ChatRoom r : rooms.getAll()){
            ChatUser user = r.getUser(sessionId);
            if(null != user){
                r.unjoinUser(sessionId);
                // send message to all users in the room
                ChatMessage message = new ChatMessage(ChatMessage.Type.CHAT_PRESENCE);
                message.set(MessageProperty.USERNAME, user.getUsername());
                message.set(MessageProperty.STAGE, LEAVE_ROOM);
                sendMessageForRoom(message, r);
            }
        }
    }
}
