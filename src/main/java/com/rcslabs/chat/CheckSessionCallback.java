package com.rcslabs.chat;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import com.rcslabs.a3.auth.IAuthController;
import com.rcslabs.a3.auth.ISession;
import com.rcslabs.a3.auth.Session;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by sx on 01.07.14.
 */
public class CheckSessionCallback implements FutureCallback<HttpResponse> {

    protected final static Logger log = LoggerFactory.getLogger(CheckSessionCallback.class);

    private final IAuthController authController;
    private final ISession session;

    public CheckSessionCallback(ISession session, IAuthController authController){
        this.authController = authController;
        this.session = session;
    }

    @Override
    public void completed(HttpResponse result) {
        log.debug("Response: " + result.getStatusLine());

        if(200 != result.getStatusLine().getStatusCode()){
            authController.onSessionFailed(session, ""+result.getStatusLine().getStatusCode());
            return;
        }

        HttpEntity entity = result.getEntity();
        try {
            String inputLine, json = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
            while ((inputLine = in.readLine()) != null) {
                json += inputLine;
            }
            in.close();

            Gson gson = new Gson();
            StringMap _result = gson.fromJson(json, StringMap.class);

            if(_result.containsKey("sessionId")){
                ((Session)session).setSessionId((String) _result.get("sessionId"));
                authController.onSessionStarted(session);
            } else if (_result.containsKey("error")){
                authController.onSessionFailed(session, (String) _result.get("error"));
            } else {
                authController.onSessionFailed(session, "Unknown JSON entity");
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            authController.onSessionFailed(session, "Unable to check session ID");
        }
    }

    @Override
    public void failed(Exception e) {
        log.error(e.getMessage(), e);
        authController.onSessionFailed(session, "Unable to check session ID");
    }

    @Override
    public void cancelled() {
        log.warn("HTTP request was cancelled");
        authController.onSessionFailed(session, "Session ID check was cancelled");
    }
}
