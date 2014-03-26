package com.rcslabs.webcall;

import com.rcslabs.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Properties;

public class Config implements IConfig{

	protected final static Logger log = LoggerFactory.getLogger(Config.class);

	private AbstractConfigChainHandler firstHandler;

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
        AbstractConfigChainHandler argsHandler = new ArgsConfigChainHandler();
        FileConfigChainHandler fileHandler = new FileConfigChainHandler();
        DefaultsConfigChainHandler defaultsHandler = new DefaultsConfigChainHandler();
		if(args.containsKey("config")){
            fileHandler.readFile(args.getProperty("config"));
		}
        // create chain
        argsHandler.setNext(fileHandler);
        fileHandler.setNext(defaultsHandler);
        this.firstHandler = argsHandler;

        RedisConfigChainHandler redisHandler = new RedisConfigChainHandler(getMessagingHost(), getMessagingPort());
        // create chain again :)
        argsHandler.setNext(redisHandler);
        redisHandler.setNext(fileHandler);
        fileHandler.setNext(defaultsHandler);
	}
	
	@Override
	public String getDatabaseUrl() {
		return firstHandler.getDatabaseUrl();
	}

	@Override
	public String getDatabaseUser() {
		return firstHandler.getDatabaseUser();
	}

	@Override
	public String getDatabasePassword() {
		return firstHandler.getDatabasePassword();
	}

    @Override
	public String getSipLocalHost() {
		return firstHandler.getSipLocalHost();
	}

	@Override
	public Integer getSipLocalPort() {
		return firstHandler.getSipLocalPort();
	}

	@Override
	public String getSipServerHost() {
		return firstHandler.getSipServerHost();
	}

	@Override
	public Integer getSipServerPort() {
		return firstHandler.getSipServerPort();
	}

	@Override
	public String getSipProxyHost() {
		return firstHandler.getSipProxyHost();
	}

	@Override
	public Integer getSipProxyPort() {
		return firstHandler.getSipProxyPort();
	}
	
	@Override
	public Integer getSipExpires() {
		return firstHandler.getSipExpires();
	}

	@Override
	public String getSipUserAgent() {
		return firstHandler.getSipUserAgent();
	}

	@Override
	public String getMcChannel() {
		return firstHandler.getMcChannel();
	}
	
	@Override
	public String getMessagingHost() {
		return firstHandler.getMessagingHost();
	}

	@Override
	public Integer getMessagingPort() {
		return firstHandler.getMessagingPort();
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
