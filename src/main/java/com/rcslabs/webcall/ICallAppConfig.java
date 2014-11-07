package com.rcslabs.webcall;

import com.rcslabs.a3.config.IConfig;
import com.rcslabs.a3.config.IDatabaseConfig;
import com.rcslabs.a3.config.ISIPConfig;

public interface ICallAppConfig extends IConfig, IDatabaseConfig, ISIPConfig {

    public String getMcChannel();

    public String getApiAuthCheckUrl();
}
