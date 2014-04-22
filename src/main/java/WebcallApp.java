import com.rcslabs.a3.IApplication;
import com.rcslabs.a3.messaging.IMessageBrokerDelegate;
import com.rcslabs.chat.BaseChatApplication;
import com.rcslabs.a3.auth.AuthMessage;
import com.rcslabs.webcall.calls.CallMessage;
import com.rcslabs.webcall.media.MediaMessage;
import com.rcslabs.a3.messaging.IMessageBroker;
import com.rcslabs.a3.messaging.MessageMarshaller;
import com.rcslabs.a3.messaging.RedisMessageBroker;
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

    private MessageMarshaller m;

	void run() throws Exception
	{
        try{
            ICallAppConfig config = new CallAppConfig();
			log.info(config.toString());
						
			broker = new RedisMessageBroker(config.getMessagingHost(), config.getMessagingPort());

            MessageMarshaller m = MessageMarshaller.getInstance();
            m.registerMessageClass(AuthMessage.class);
            m.registerMessageClass(CallMessage.class);
            m.registerMessageClass(MediaMessage.class);
            m.start();

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

            registerApplication(new ConstructorCallApplication("constructor", config, broker, factory));
            registerApplication(new BaseCallApplication("click2call", config, broker, factory));
            registerApplication(new BaseChatApplication("chat", broker));
		}catch(Exception e){
            log.error("Unhandled exception in main. Application will be exit.", e);
			return; 
		}
	}

    void registerApplication(IApplication app)
    {
        if(app.ready()){
            log.info("Register " + app.getClass() + " for channel " + app.getMessagingChannel());
            broker.subscribe(app.getMessagingChannel(), app);
        } else {
            log.error("Unable to register " + app.getClass() + " for channel " + app.getMessagingChannel());
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
