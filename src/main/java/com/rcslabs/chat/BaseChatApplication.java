package com.rcslabs.chat;

import com.rcslabs.a3.IDataStorage;
import com.rcslabs.a3.InMemoryDataStorage;
import com.rcslabs.a3.auth.*;
import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.exception.InvalidMessageException;
import com.rcslabs.a3.messaging.IMessage;
import com.rcslabs.a3.messaging.IMessageBroker;
import com.rcslabs.webcall.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sx on 22.04.14.
 */
public class BaseChatApplication implements IChatApplication {

    protected final static Logger log = LoggerFactory.getLogger(BaseChatApplication.class);

    protected final String messagingChannel;
    protected final IMessageBroker broker;
    protected final IAuthController authController;
    protected final IDataStorage<ChatRoom> rooms;
    protected final IDataStorage<ChatMessage> messages;

    public BaseChatApplication(String messagingChannel, IMessageBroker broker) {
        this.messagingChannel = messagingChannel;
        this.broker = broker;
        this.rooms = new InMemoryDataStorage<>();
        this.messages = new InMemoryDataStorage<>();
        this.authController = new ChatAuthController(broker, new InMemorySessionStorage(), 3600);
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
                authController.closeSession((String) message.get("sessionId"));
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
        } catch (Exception e){
            log.error(e.getMessage(), e);
            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.JOIN_CHATROOM_FAILED);
        } finally {
            broker.publish(message.getClientChannel(), response);
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
            room.unjoinUser(sessionId);
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
            String sender = (String)message.get(MessageProperty.SENDER);
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

            for(ChatUser u : room.getAllUsers()){
                if(u.equals(user)){ continue; }
                ChatMessage messageTo = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.INCOMING_MESSAGE);
                messageTo.set(MessageProperty.SESSION_ID, u.getSessionId());
                broker.publish(messageTo.getClientChannel(), messageTo);
            }

            messages.set(messageId, message); // TODO: linked set

            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.CHAT_MESSAGE_SENT);
        } catch (Exception e){
            log.error(e.getMessage(), e);
            response = (ChatMessage)message.cloneWithAnyType(ChatMessage.Type.CHAT_MESSAGE_FAILED);
        } finally {
            broker.publish(message.getClientChannel(), response);
        }
    }
}
