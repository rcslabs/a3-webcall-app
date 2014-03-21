package com.rcslabs.media;


import com.rcslabs.webcall.AlenaMessage;

public interface IMediaPointDelegate {
    void onSdpOffererReceived(IMediaPoint mp, AlenaMessage message);
    void onSdpAnswererReceived(IMediaPoint mp, AlenaMessage message);
    void onMediaPointCreated(IMediaPoint mp, AlenaMessage message);
    void onMediaPointJoinedToRoom(IMediaPoint mp, AlenaMessage message);
    void onMediaPointUnjoinedFromRoom(IMediaPoint mp, AlenaMessage message);
    void onMediaFailed(IMediaPoint mp, AlenaMessage message);
}
