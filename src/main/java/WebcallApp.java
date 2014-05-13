import com.rcslabs.a3.IApplication;
import com.rcslabs.a3.IController;
import com.rcslabs.a3.auth.AuthMessage;
import com.rcslabs.a3.messaging.MessageMarshaller;
import com.rcslabs.a3.messaging.RedisConnector;
import com.rcslabs.chat.BaseChatApplication;
import com.rcslabs.chat.ChatMessage;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.JainSipRclFactory;
import com.rcslabs.webcall.BaseCallApplication;
import com.rcslabs.webcall.CallAppConfig;
import com.rcslabs.webcall.ConstructorCallApplication;
import com.rcslabs.webcall.ICallAppConfig;
import com.rcslabs.webcall.calls.CallMessage;
import com.rcslabs.webcall.media.MediaMessage;
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

    private static RedisConnector redisConnector;

    private static List<IApplication> apps;
    private static List<IController> cntrls;

	void run()
	{
        apps = new ArrayList<>();

        ICallAppConfig config = new CallAppConfig();
        redisConnector = new RedisConnector(config.getRedisUri());
        config.initWithRedis(redisConnector);
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

        MessageMarshaller m = MessageMarshaller.getInstance();
        m.registerMessageClass(AuthMessage.class);
        m.registerMessageClass(CallMessage.class);
        m.registerMessageClass(MediaMessage.class);
        m.registerMessageClass(ChatMessage.class);
        m.start();

        //cntrls.add();

        apps.add(new ConstructorCallApplication(redisConnector, "constructor", config, factory));
        apps.add(new BaseCallApplication(redisConnector, "click2call", config, factory));
        apps.add(new BaseChatApplication(redisConnector, "chat"));

        for(IApplication a : apps){ a.start(); }
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
