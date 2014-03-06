package com.rcslabs.webcall;

import java.util.Set;

public interface IConfig {

	public abstract String getSipLocalHost();
	public abstract Integer getSipLocalPort();
	public abstract String getSipServerHost();
	public abstract Integer getSipServerPort();
	public abstract String getSipProxyHost();
	public abstract Integer getSipProxyPort();
	public abstract Integer getSipExpires();
	public abstract String getSipUserAgent();
	public abstract String getMessagingHost();
	public abstract Integer getMessagingPort();
	public abstract String getDatabaseUrl();
	public abstract String getDatabaseUser();
	public abstract String getDatabasePassword();
    public abstract String getMcChannel();
}
