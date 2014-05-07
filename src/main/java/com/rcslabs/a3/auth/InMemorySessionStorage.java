package com.rcslabs.a3.auth;

import com.rcslabs.a3.InMemoryDataStorage;

import java.util.Iterator;

/**
 * Created by sx on 17.04.14.
 */
public class InMemorySessionStorage extends InMemoryDataStorage<ISession> implements ISessionStorage {

    @Override
    public ISession findPreviousSession(ISession session)
    {
        Iterator<String> it = data.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ISession exist = data.get(key);
            if(exist.getSessionId().equals(session.getSessionId())){ continue; }
            if(!exist.getUsername().equals(session.getUsername())){ continue; }
            it.remove();
            return exist;
        }

        return null;
    }
}
