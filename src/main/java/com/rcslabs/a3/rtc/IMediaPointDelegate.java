package com.rcslabs.a3.rtc;


import com.rcslabs.a3.messaging.IMessage;

public interface IMediaPointDelegate {
    void onSdpOffererReceived(IMediaPoint mp, IMessage message);
    void onSdpAnswererReceived(IMediaPoint mp, IMessage message);
    void onMediaPointCreated(IMediaPoint mp, IMessage message);
    void onMediaPointJoinedToRoom(IMediaPoint mp, IMessage message);
    void onMediaPointUnjoinedFromRoom(IMediaPoint mp, IMessage message);
    void onMediaFailed(IMediaPoint mp, IMessage message);
}
