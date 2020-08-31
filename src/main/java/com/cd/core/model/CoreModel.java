package com.cd.core.model;

import com.cd.core.cache.CoreLocalCache;
import com.cd.core.cache.impl.CoreLocalCacheImpl;

public class CoreModel {
    private CoreLocalCache localCache = CoreLocalCacheImpl.newInstance();
    private volatile static CoreModel model;
    private CoreModel(){}

    public static CoreModel newInstance(){
        if (model == null){
            synchronized (CoreModel.class){
                if (model == null){
                    model = new CoreModel();
                }
            }
        }
        return model;
    }

    public boolean goOnline(String handlerId, String name){
        return localCache.putIfAbsentNameAndId(handlerId, name);
    }

    public String offLine(String id){
        String name = localCache.removeId(id);
        if (name == null){
            return null;
        }
        localCache.removeName(name);
        return name;
    }

    public boolean containsId(String id){
        return localCache.containsId(id);
    }

    public boolean containsName(String name){
        return localCache.containsName(name);
    }

}
