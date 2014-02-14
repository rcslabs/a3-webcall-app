package com.rcslabs.webcall;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config implements IConfig{

	protected final static Logger log = LoggerFactory.getLogger(Config.class);
	
	private Properties args;
	private Properties fromFile;
	private Properties defaults;
	
	public Config(){
		
		args = new Properties();
		String s = System.getProperty("sun.java.command");
		String[] s2 = s.split(" ");
		for(int i = 1; i < s2.length; ++i){
			s = s2[i].trim();
			String[] s3 = s.split("=");  
			args.setProperty(s3[0].substring(2), s3[1]);
		}
		
		defaults = new Properties();
		try {
			defaults.load(this.getClass().getClassLoader()
				.getResourceAsStream("default.properties")
			);
		} catch (IOException e) {
			log.error("Error while config defaults loading: " + e.getMessage());
		}
		
		fromFile = new Properties();
		if(args.containsKey("config")){
			try {
				fromFile.load(new FileReader(args.getProperty("config")));
			} catch (IOException e) {
				log.error("Error while config loading from file " 
			        + args.getProperty("config") + " : " + e.getMessage());
			}			
		}
	}
	
	@Override
	public String getDatabaseUrl() {
		return getPropertyAsString("db-url");
	}

	@Override
	public String getDatabaseUser() {
		return getPropertyAsString("db-user");
	}

	@Override
	public String getDatabasePassword() {
		return getPropertyAsString("db-password");
	}
	
	@Override
	public String getSipLocalHost() {
		return getPropertyAsString("sip-local-host");
	}

	@Override
	public Integer getSipLocalPort() {
		return getPropertyAsInteger("sip-local-port");
	}

	@Override
	public String getSipServerHost() {
		return getPropertyAsString("sip-server-host");
	}

	@Override
	public Integer getSipServerPort() {
		return getPropertyAsInteger("sip-server-port");
	}

	@Override
	public String getSipProxyHost() {
		return getPropertyAsString("sip-proxy-host");
	}

	@Override
	public Integer getSipProxyPort() {
		return getPropertyAsInteger("sip-proxy-port");
	}
	
	@Override
	public Integer getSipExpires() {
		return getPropertyAsInteger("sip-expires");
	}

	@Override
	public String getSipUserAgent() {
		return getPropertyAsString("sip-user-agent");
	}

	@Override
	public String getMcChannel() {
		return getPropertyAsString("mc-channel");
	}
	
	@Override
	public String getMessagingHost() {
		String mb = getPropertyAsString("messaging-uri");
		if(null == mb){ return null; }
		try {
			URI u = new URI(mb);
			return u.getHost();
		} catch (URISyntaxException e) {
			log.error("Error parse messaging URI: " + e.getMessage());
		}
		return null;
	}

	@Override
	public Integer getMessagingPort() {
		String mb = getPropertyAsString("messaging-uri");
		if(null == mb){ return null; }
		try {
			URI u = new URI(mb);
			return (-1 == u.getPort() ? 6379 : u.getPort());
		} catch (URISyntaxException e) {
			log.error("Error parse messaging URI: " + e.getMessage());
		}
		return null;
	}
	
	public void initFromFile(String filename){}

	@SuppressWarnings("unused")
	private Boolean getPropertyAsBoolean(String key){
		return Boolean.valueOf(getPropertyAsString(key));
	}

	private Integer getPropertyAsInteger(String key){
		return Integer.valueOf(getPropertyAsString(key));
	}
	
	private String getPropertyAsString(String key){
		
		if(args.containsKey(key)){
			return args.getProperty(key);
		}else if(fromFile.containsKey(key)){
			return fromFile.getProperty(key);			
		}else if(defaults.containsKey(key)){
			return defaults.getProperty(key);			
		}else{
			log.warn("No property found for key=" + key);
			return null;
		}
	}

	@Override
	public String toString() {
		String res = "Config [";
		try {
			Method[] m = IConfig.class.getDeclaredMethods();		
			for (int i = 0; i < m.length; i++) {     
				 String key = m[i].getName().substring(3);
				 String val = (m[i].invoke(this)).toString();
				 res += (key+"="+val+", ");
			}		
		} catch (Exception e) {
			log.error("Error " + e.getMessage());
		} 		
		return res + "]";
	}
}
