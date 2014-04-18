package com.rcslabs.a3.auth;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sx on 17.04.14.
 */
public class InMemorySessionStorage implements ISessionStorage {

    private final Map<String, ISession> sessions;

    public InMemorySessionStorage(){
        sessions = new ConcurrentHashMap<String, ISession>();
    }

    @Override
    public ISession findPreviousSession(ISession session)
    {
        Iterator<String> it = sessions.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ISession exist = sessions.get(key);
            if(exist.getSessionId().equals(session.getSessionId())){ continue; }
            if(!exist.getUsername().equals(session.getUsername())){ continue; }
            it.remove();
            return exist;
        }

        return null;
    }

    @Override
    public void set(String key, ISession value) {
        sessions.put(key, value);
    }

    @Override
    public ISession get(String key) {
        return sessions.get(key);
    }

    @Override
    public boolean has(String key) {
        return sessions.containsKey(key);
    }

    @Override
    public void delete(String key) {
        sessions.remove(key);
    }
}
