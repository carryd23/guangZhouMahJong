package com.cd.login.dao.impl;

import com.cd.core.dao.DaoBase;
import com.cd.login.dao.UserDao;
import com.cd.login.cache.bean.UserCacheBean;

public class UserDaoImpl extends DaoBase implements UserDao {
    @Override
    public UserCacheBean findUserByName(String name) {
        String sql = "select * from m_user where uname = ?";
        return super.querySql(UserCacheBean.class, sql, name);
    }

    @Override
    public int saveUser(UserCacheBean bean) {
        String sql = "insert into m_user(uname, pwd, gold) value(?, ?, ?)";
        return super.modifySql(sql, bean.getUname(), bean.getPwd(), bean.getGold());
    }

    @Override
    public int updateUser(UserCacheBean bean) {
        String sql = "update m_user set pwd = ?, gold = ? where uid = ?";
        return super.modifySql(sql, bean.getPwd(), bean.getGold(), bean.getUid());
    }
}
