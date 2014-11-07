import com.rcslabs.a3.IApplication;
import com.rcslabs.a3.messaging.*;
import com.rcslabs.chat.BaseChatApplication;
import com.rcslabs.chat.ChatMessage;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.JainSipRclFactory;
import com.rcslabs.webcall.BaseCallApplication;
import com.rcslabs.webcall.CallAppConfig;
import com.rcslabs.webcall.ConstructorCallApplication;
import com.rcslabs.webcall.ICallAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * --sip-local-host=192.168.1.40 --sip-server-host=192.168.1.200 --sip-proxy-host=192.168.1.200 \
 * --redis-uri=redis://192.168.1.38:6379 --mc-channelName=media-controller \
 * --db-url=jdbc:postgresql://localhost/webcallr2 --db-user=rcslabs --db-password=rcslabs123
 * @author sx
 *
 */
public class WebcallApp{

	private final static Logger log = LoggerFactory.getLogger(WebcallApp.class);

    private RedisFactory redis;
    private List<IApplication> apps;

    void run() throws Exception {
        apps = new ArrayList<>();

        ICallAppConfig config = new CallAppConfig();
        log.info(config.toString());

        JainSipGlobalParams params = new JainSipGlobalParams();
        params.setLocalIpAddress(  config.getSipLocalHost() );
        params.setLocalPort(       config.getSipLocalPort() );
        params.setSipServerAddress(config.getSipServerHost());
        params.setSipServerPort(   config.getSipServerPort());
        params.setSipProxyAddress( config.getSipProxyHost() );
        params.setSipProxyPort(    config.getSipProxyPort() );
        params.setExpires(         config.getSipExpires()   );
        params.setSipUserAgent(    config.getSipUserAgent() );

        JainSipRclFactory factory = new JainSipRclFactory(params);

        redis = new RedisFactory(config.getRedisUri());
        redis.init();

        JsonMessageSerializer s = new JsonMessageSerializer();
        s.registerMessageClass(AuthMessage.class);
        s.registerMessageClass(CallMessage.class);
        s.registerMessageClass(MediaMessage.class);
        s.registerMessageClass(ChatMessage.class);
        redis.getConnector().addSerializer(s);

        apps.add(new ConstructorCallApplication("constructor", redis.getConnector(), config, factory));
        apps.add(new BaseCallApplication("click2call", redis.getConnector(), config, factory));
        apps.add(new BaseChatApplication("chat", redis.getConnector(), config));
        for(IApplication a : apps){ a.init(); }
	}

	public static void main(String[] args) {	
		try {			
			new WebcallApp().run();
		} catch (Exception e) {
            log.error("Unhandled exception in main. Application will be exit.");
            log.error(e.getMessage(), e);
            System.exit(1);
		}
	}	
}
