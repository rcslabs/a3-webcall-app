package com.rcslabs.a3.config;

import com.rcslabs.webcall.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public abstract class AbstractConfig implements IConfigChainHandler {

	protected final static Logger log = LoggerFactory.getLogger(AbstractConfig.class);

	private AbstractConfigChainHandler firstHandler;

	public AbstractConfig(){
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

        RedisConfigChainHandler redisHandler = new RedisConfigChainHandler(getMessagingHostInternal(), getMessagingPortInternal());
        // create chain again :)
        argsHandler.setNext(redisHandler);
        redisHandler.setNext(fileHandler);
        fileHandler.setNext(defaultsHandler);
	}

    @Override
    public void setNext(IConfigChainHandler next) {
        throw new RuntimeException("You must not access this operation");
    }

    @Override
    public Boolean getPropertyAsBoolean(String key) {
        return firstHandler.getPropertyAsBoolean(key);
    }

    @Override
    public Integer getPropertyAsInteger(String key) {
        return firstHandler.getPropertyAsInteger(key);
    }

    @Override
    public String getPropertyAsString(String key) {
        return firstHandler.getPropertyAsString(key);
    }

    protected String getMessagingHostInternal() {
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

    protected Integer getMessagingPortInternal() {
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
