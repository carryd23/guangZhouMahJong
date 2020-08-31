package com.cd.core.interceptor.impl;

import com.cd.core.cache.CoreLocalCache;
import com.cd.core.cache.impl.CoreLocalCacheImpl;
import com.cd.core.interceptor.ActionInterceptor;

public class ActionInterceptorImpl implements ActionInterceptor {
    private CoreLocalCache localCache = CoreLocalCacheImpl.newInstance();

    @Override
    public boolean logged(String handlerId) {
        return localCache.containsId(handlerId);
    }
}
