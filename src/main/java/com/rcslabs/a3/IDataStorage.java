package com.rcslabs.a3;

/**
 * Created by sx on 17.04.14.
 */
public interface IDataStorage<T> {

    void set(String key, T value);

    T get(String key);

    boolean has(String key);

    void delete(String key);
}
