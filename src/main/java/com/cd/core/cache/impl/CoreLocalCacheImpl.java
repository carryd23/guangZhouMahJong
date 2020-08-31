package com.cd.core.cache.impl;

import com.cd.core.cache.CoreLocalCache;

import java.util.concurrent.ConcurrentHashMap;

public class CoreLocalCacheImpl implements CoreLocalCache {
    private ConcurrentHashMap<String, String> name2Id = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> id2Name = new ConcurrentHashMap<>();
    private static CoreLocalCacheImpl localCache;

    private CoreLocalCacheImpl(){}

    public static CoreLocalCacheImpl newInstance(){
        if (localCache == null){
            synchronized (CoreLocalCacheImpl.class){
                if (localCache == null){
                    localCache = new CoreLocalCacheImpl();
                }
            }
        }
        return localCache;
    }

    @Override
    public String getNameById(String id) {
        return id2Name.get(id);
    }

    @Override
    public String getIdByName(String name) {
        return name2Id.get(name);
    }

    @Override
    public boolean putIfAbsentNameAndId(String id, String name) {
        String id2 = id2Name.putIfAbsent(id, name);
        String name2 = name2Id.putIfAbsent(name, id);
        return id2 == null && name2 == null;
    }

    @Override
    public boolean containsId(String id) {
        return id2Name.containsKey(id);
    }

    @Override
    public boolean containsName(String name) {
        return name2Id.containsKey(name);
    }

    @Override
    public String removeId(String id) {
        return id2Name.remove(id);
    }

    @Override
    public String removeName(String name) {
        return name2Id.remove(name);
    }
}
