package com.rcslabs.a3;

/**
 * Created by sx on 17.04.14.
 */
public interface IDataHolder {

    void set(String key, Object value);

    Object get(String key);

    boolean has(String key);

    void delete(String key);
}
