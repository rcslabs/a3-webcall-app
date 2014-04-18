package com.rcslabs.a3.auth;

import com.rcslabs.a3.IDataStorage;

/**
 * Created by sx on 17.04.14.
 */
public interface ISessionStorage extends IDataStorage<ISession> {

    ISession findPreviousSession(ISession session);
}
