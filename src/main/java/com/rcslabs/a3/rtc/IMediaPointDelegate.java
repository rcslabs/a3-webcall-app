package com.rcslabs.a3.rtc;


import com.rcslabs.a3.messaging.IAlenaMessage;

public interface IMediaPointDelegate {
    void onSdpOffererReceived(IMediaPoint mp, IAlenaMessage message);
    void onSdpAnswererReceived(IMediaPoint mp, IAlenaMessage message);
    void onMediaPointCreated(IMediaPoint mp, IAlenaMessage message);
    void onMediaPointJoinedToRoom(IMediaPoint mp, IAlenaMessage message);
    void onMediaPointUnjoinedFromRoom(IMediaPoint mp, IAlenaMessage message);
    void onMediaFailed(IMediaPoint mp, IAlenaMessage message);
}
