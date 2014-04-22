package com.rcslabs.webcall;

import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.config.IDatabaseConfig;
import com.rcslabs.a3.config.ISipConfig;

public interface ICallAppConfig extends IConfig, IDatabaseConfig, ISipConfig {

    public String getMcChannel();
}
