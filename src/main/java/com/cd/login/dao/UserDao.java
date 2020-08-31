package com.cd.login.dao;

import com.cd.login.cache.bean.UserCacheBean;

public interface UserDao {
    UserCacheBean findUserByName(String name);

    int saveUser(UserCacheBean bean);

    int updateUser(UserCacheBean bean);
}
