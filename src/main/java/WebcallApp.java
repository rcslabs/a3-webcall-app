import com.rcslabs.messaging.IMessageBroker;
import com.rcslabs.messaging.RedisMessageBroker;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.JainSipRclFactory;
import com.rcslabs.webcall.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * --sip-local-host=192.168.1.40 --sip-server-host=192.168.1.200 --sip-proxy-host=192.168.1.200 \
 * --messaging-uri=redis://192.168.1.38:6379 --mc-channel=media-controller \
 * --db-url=jdbc:postgresql://localhost/webcallr2 --db-user=rcslabs --db-password=rcslabs123
 * @author sx
 *
 */
public class WebcallApp{

	private final static Logger log = LoggerFactory.getLogger(WebcallApp.class);

    private static IMessageBroker broker;

	void run() throws Exception
	{
        IConfig config = null;

        try{
            config = new Config();
			log.info(config.toString());
						
			broker = new RedisMessageBroker(config.getMessagingHost(), config.getMessagingPort());

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

            registerApplication(new ConstructorApplication("constructor", config, broker, factory));
            registerApplication(new BaseApplication("click2call", config, broker, factory));

		}catch(Exception e){
            log.error("Unhandled exception in main. Application will be exit.", e);
			return; 
		}
	}

    void registerApplication(ICallApplication app)
    {
        if(app.ready()){
            log.info("Register " + app.getClass() + " for channel " + app.getChannelName());
            broker.subscribe(app.getChannelName(), app);
        } else {
            log.error("Unable to register " + app.getClass() + " for channel " + app.getChannelName());
        }
    }

	public static void main(String[] args) {	
		try {			
			new WebcallApp().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
