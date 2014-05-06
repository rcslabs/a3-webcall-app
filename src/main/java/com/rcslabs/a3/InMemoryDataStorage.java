package com.rcslabs.a3;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sx on 06.05.14.
 */
public class InMemoryDataStorage<T> implements IDataStorage<T> {

    protected final Map<String, T> data;

    public InMemoryDataStorage(){
        data = new ConcurrentHashMap<>();
    }

    @Override
    public void set(String key, T value) {
        data.put(key, value);
    }

    @Override
    public T get(String key) {
        return data.get(key);
    }

    @Override
    public Collection<T> getAll() {
        return data.values();
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    public void delete(String key) {
        data.remove(key);
    }

}
