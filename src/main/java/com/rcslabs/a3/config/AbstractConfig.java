package com.rcslabs.a3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public abstract class AbstractConfig implements IConfigChainHandler, IConfig {

	protected final Logger log;

	private AbstractConfigChainHandler firstHandler;
    private ArgsConfigChainHandler argsHandler;
    private FileConfigChainHandler fileHandler;
    private DefaultsConfigChainHandler defaultsHandler;

    protected AbstractConfig(){
        log = LoggerFactory.getLogger(getClass());

        Properties args = new Properties();
		String s = System.getProperty("sun.java.command");
		String[] s2 = s.split(" ");
		for(int i = 1; i < s2.length; ++i){
			s = s2[i].trim();
			String[] s3 = s.split("=");
            if(s3.length < 2){ continue; }
			args.setProperty(s3[0].substring(2), s3[1]);
		}

        // init chain handlers
        argsHandler = new ArgsConfigChainHandler();
        fileHandler = new FileConfigChainHandler();
        defaultsHandler = new DefaultsConfigChainHandler();
		if(args.containsKey("config")){
            fileHandler.readFile(args.getProperty("config"));
		}
        // create chain
        argsHandler.setNext(fileHandler);
        fileHandler.setNext(defaultsHandler);
        this.firstHandler = argsHandler;
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

    @Override
    public URI getRedisUri() {
        String mb = getPropertyAsString("redis-uri");
        if(null == mb){ return null; }
        try {
            return new URI(mb);
        } catch (URISyntaxException e) {
            log.error("Error parse Redis URI: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        String res = "Config [";
        try {
            Method[] m = getClass().getMethods();
            for (int i = 0; i < m.length; i++) {
                String methodName = m[i].getName();
                if(0 != methodName.indexOf("get")){ continue; }
                if(0 == methodName.indexOf("getProperty")){ continue; }
                if(0 == methodName.indexOf("getClass")){ continue; }
                String key = methodName.substring(3);
                Object val = (m[i].invoke(this));
                res += (key+"="+val+", ");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return res + "]";
    }
}
