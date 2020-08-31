package com.cd.login.cache;

import com.cd.login.cache.bean.UserCacheBean;

public interface UserCache {
    String saveUserCacheBean(UserCacheBean bean);

    UserCacheBean findUserCacheBeanByName(String name);

    long delUserCacheBeanByName(String name);
}
