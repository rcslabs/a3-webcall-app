package com.rcslabs.webcall.media;

import com.rcslabs.a3.messaging.Message;

/**
 * Created by sx on 15.04.14.
 */
public class MediaMessage extends Message<MediaMessage.Type> {

    public static enum Type {
        SDP_OFFER,
        SDP_ANSWER,
        CREATE_MEDIA_POINT,
        CREATE_MEDIA_POINT_OK,
        CREATE_MEDIA_POINT_FAILED,
        REMOVE_MEDIA_POINT,
        REMOVE_MEDIA_POINT_OK,
        REMOVE_MEDIA_POINT_FAILED,
        JOIN_ROOM,
        JOIN_OK,
        JOIN_FAILED,
        UNJOIN_ROOM,
        UNJOIN_OK,
        UNJOIN_FAILED,
        CRITICAL_ERROR
    }

    public MediaMessage(Type type){
        super(type);
    }
}
