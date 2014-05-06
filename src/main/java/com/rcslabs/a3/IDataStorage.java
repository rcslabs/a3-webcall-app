package com.rcslabs.a3;

import java.util.Collection;

/**
 * Created by sx on 17.04.14.
 */
public interface IDataStorage<T> {

    void set(String key, T value);

    T get(String key);

    Collection<T> getAll();

    boolean has(String key);

    void delete(String key);
}
