package com.cd.login.cache.bean;

public class UserCacheBean {
    private int uid;
    private String uname;
    private String pwd;
    private long gold;

    public int getUid() {
        return uid;
    }

    public UserCacheBean setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public String getUname() {
        return uname;
    }

    public UserCacheBean setUname(String uname) {
        this.uname = uname;
        return this;
    }

    public String getPwd() {
        return pwd;
    }

    public UserCacheBean setPwd(String pwd) {
        this.pwd = pwd;
        return this;
    }

    public long getGold() {
        return gold;
    }

    public UserCacheBean setGold(long gold) {
        this.gold = gold;
        return this;
    }
}
