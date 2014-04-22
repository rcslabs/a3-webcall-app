package com.rcslabs.webcall.media;

import com.rcslabs.a3.rtc.IMediaContext;
import com.rcslabs.webcall.ICallAppConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sx on 06.03.14.
 */
public class StaticMediaContext implements IMediaContext {

    private ICallAppConfig config;

    public StaticMediaContext(ICallAppConfig config) {
        this.config = config;
    }

    @Override
    public String getMcChannel() {
        return config.getMcChannel();
    }

    @Override
    public Set getRtpVideoCodecs() {
        Set s = new HashSet();
        s.add("VP8/90000");
        s.add("H264/90000");
        s.add("H263/90000");
        return s;
    }

    @Override
    public Set getRtpAudioCodecs() {
        Set s = new HashSet();
        s.add("PCMA/8000");
        return s;
    }
}
