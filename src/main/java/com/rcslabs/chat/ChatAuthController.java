package com.rcslabs.chat;

import com.rcslabs.a3.IDataStorage;
import com.rcslabs.a3.auth.AbstractAuthController;
import com.rcslabs.a3.messaging.AuthMessage;
import com.rcslabs.a3.auth.ISession;
import com.rcslabs.webcall.ICallAppConfig;
import com.rcslabs.redis.RedisConnector;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by sx on 22.04.14.
 */
public class ChatAuthController extends AbstractAuthController {

    private static String API_AUTH_CHECK ;//= "http://test-ru.luxmsbi.com/bi-fe-server/api/auth/check";

    public ChatAuthController(RedisConnector redisConnector, ICallAppConfig config, IDataStorage<ISession> storage) {
        super("auth:chat", redisConnector, storage);
        API_AUTH_CHECK = config.getApiAuthCheckUrl();
    }

    @Override
    public void onAuthMessage(AuthMessage message) {

    }

    @Override
    public void startSession(ISession session) {
        log.debug("Check session " + session.getPassword() + " using " + API_AUTH_CHECK);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            httpclient.start();
            HttpPost request = new HttpPost(API_AUTH_CHECK);
            final String JSON_STRING = "{\"sessionId\":\""+session.getPassword()+"\"}";
            request.setEntity(new StringEntity(JSON_STRING, "UTF-8"));
            request.setHeader("Content-Type", "application/json");
            Future<HttpResponse> future = httpclient.execute(request, new CheckSessionCallback(session, this));
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onSessionStarted(ISession session) {
        storage.set(session.getSessionId(), session);
        super.onSessionStarted(session);
    }

    @Override
    public void closeSession(String sessionId) {
        ISession session = storage.get(sessionId);
        if(null == session) return;
        storage.delete(sessionId);
        super.onSessionClosed(session);
    }
}
