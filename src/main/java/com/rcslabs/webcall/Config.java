package com.rcslabs.webcall;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.rcslabs.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config implements IConfig{

	protected final static Logger log = LoggerFactory.getLogger(Config.class);

	private IConfigChainHandler firstHandler;

	public Config(){
        Properties args = new Properties();
		String s = System.getProperty("sun.java.command");
		String[] s2 = s.split(" ");
		for(int i = 1; i < s2.length; ++i){
			s = s2[i].trim();
			String[] s3 = s.split("=");  
			args.setProperty(s3[0].substring(2), s3[1]);
		}

        // init chain handlers
        firstHandler = new ArgsConfigChainHandler();
        FileConfigChainHandler fileHandler = new FileConfigChainHandler();
		if(args.containsKey("config")){
            fileHandler.readFile(args.getProperty("config"));
		}
        DefaultsConfigChainHandler defaultsHandler = new DefaultsConfigChainHandler();
        RedisConfigChainHandler redisHandler = new RedisConfigChainHandler(getMessagingHost(), getMessagingPort());

        // create chain
        firstHandler.setNext(redisHandler);
        redisHandler.setNext(fileHandler);
        fileHandler.setNext(defaultsHandler);
	}
	
	@Override
	public String getDatabaseUrl() {
		return firstHandler.getPropertyAsString("db-url");
	}

	@Override
	public String getDatabaseUser() {
		return firstHandler.getPropertyAsString("db-user");
	}

	@Override
	public String getDatabasePassword() {
		return firstHandler.getPropertyAsString("db-password");
	}
	
	@Override
	public String getSipLocalHost() {
		return firstHandler.getPropertyAsString("sip-local-host");
	}

	@Override
	public Integer getSipLocalPort() {
		return firstHandler.getPropertyAsInteger("sip-local-port");
	}

	@Override
	public String getSipServerHost() {
		return firstHandler.getPropertyAsString("sip-server-host");
	}

	@Override
	public Integer getSipServerPort() {
		return firstHandler.getPropertyAsInteger("sip-server-port");
	}

	@Override
	public String getSipProxyHost() {
		return firstHandler.getPropertyAsString("sip-proxy-host");
	}

	@Override
	public Integer getSipProxyPort() {
		return firstHandler.getPropertyAsInteger("sip-proxy-port");
	}
	
	@Override
	public Integer getSipExpires() {
		return firstHandler.getPropertyAsInteger("sip-expires");
	}

	@Override
	public String getSipUserAgent() {
		return firstHandler.getPropertyAsString("sip-user-agent");
	}

	@Override
	public String getMcChannel() {
		return firstHandler.getPropertyAsString("mc-channel");
	}
	
	@Override
	public String getMessagingHost() {
		String mb = firstHandler.getPropertyAsString("messaging-uri");
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
		String mb = firstHandler.getPropertyAsString("messaging-uri");
		if(null == mb){ return null; }
		try {
			URI u = new URI(mb);
			return (-1 == u.getPort() ? 6379 : u.getPort());
		} catch (URISyntaxException e) {
			log.error("Error parse messaging URI: " + e.getMessage());
		}
		return null;
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
