package com.cd.login.cmds;

import com.cd.core.anno.Cmd;
import com.cd.core.data.MessageData;
import com.cd.login.cache.UserCache;
import com.cd.login.cache.bean.UserCacheBean;
import com.cd.login.cache.impl.UserCacheImpl;

@Cmd("show")
public class ShowCmd {
    private UserCache userCache = new UserCacheImpl();
    @Cmd("show")
    public MessageData show(String name){
        UserCacheBean bean = userCache.findUserCacheBeanByName(name);
        return new MessageData("show.msg").setArgs(bean.getUid(), bean.getUname(), bean.getGold());
    }
}
