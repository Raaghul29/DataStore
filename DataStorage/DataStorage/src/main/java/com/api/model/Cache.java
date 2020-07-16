package com.api.model;

public interface Cache {
    
    void add(String key, String directory, long periodInMillis);
 
    void remove(String key);
 
    Object get(String key);
    
    Object getDeadKey(String key);
 
    void clear();
 
    long size();
    
}

