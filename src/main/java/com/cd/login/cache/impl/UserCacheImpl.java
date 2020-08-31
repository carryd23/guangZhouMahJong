package com.cd.login.cache.impl;

import com.cd.core.cache.CacheBase;
import com.cd.login.cache.UserCache;
import com.cd.login.cache.bean.UserCacheBean;

public class UserCacheImpl extends CacheBase implements UserCache {
    @Override
    public String saveUserCacheBean(UserCacheBean bean) {
        return super.saveObject(bean.getUname(), bean);
    }

    @Override
    public UserCacheBean findUserCacheBeanByName(String name) {
        return super.getObject(UserCacheBean.class, name);
    }

    @Override
    public long delUserCacheBeanByName(String name) {
        return super.delKey(name);
    }
}
