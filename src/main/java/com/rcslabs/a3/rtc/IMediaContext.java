package com.rcslabs.a3.rtc;

import java.util.Set;

/**
 * Created by sx on 06.03.14.
 */
public interface IMediaContext {
    public abstract String getMcChannel();
    public abstract Set getRtpVideoCodecs();
    public abstract Set getRtpAudioCodecs();
}
