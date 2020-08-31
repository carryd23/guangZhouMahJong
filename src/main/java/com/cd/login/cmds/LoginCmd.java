package com.cd.login.cmds;

import com.cd.core.anno.Cmd;
import com.cd.core.data.MessageData;
import com.cd.core.log.LogUtil;
import com.cd.login.cache.UserCache;
import com.cd.login.cache.impl.UserCacheImpl;
import com.cd.login.dao.UserDao;
import com.cd.login.cache.bean.UserCacheBean;
import com.cd.login.dao.impl.UserDaoImpl;
import com.cd.login.utils.LoginUtil;
import com.cd.room.model.RoomModel;
import com.cd.room.model.impl.RoomModelImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cd.core.utils.ConstantUtil.SIGN_OUT;

@Cmd("login")
public class LoginCmd {
    private UserDao userDao = new UserDaoImpl();
    private UserCache userCache = new UserCacheImpl();
    private RoomModel roomModel = new RoomModelImpl();

    /**
     * 判断登录用户合法性.合法则记录该登录用户
     * @param handlerId id
     * @param name 用户名
     * @param pwd 密码
     * @return tip
     */
    @Cmd("login")
    public MessageData login(String handlerId, String name, String pwd){
        boolean repeat = LoginUtil.actionRepeat(handlerId);
        if (repeat){
            return new MessageData("login.login.fail.repeatLogin");
        }
        UserCacheBean bean = userDao.findUserByName(name);
        if (bean == null){
            return new MessageData("login.login.fail.notExist");
        }
        if (!bean.getPwd().equals(pwd)){
            return new MessageData("login.login.fail.mismatch");
        }
        boolean logged = LoginUtil.alreadyLogged(bean.getUname());
        if (logged){
            return new MessageData("login.login.fail.logged");
        }
        String s = userCache.saveUserCacheBean(bean);
        if (!"OK".equals(s)){
            LogUtil.error(LoginCmd.class, "log.error.login.login.redis", name);
            return new MessageData("system.error");
        }
        //合法，记录handlerId与该用户信息
        boolean online = LoginUtil.goOnline(handlerId, name);
        if (!online){
            LogUtil.error(LoginCmd.class, "log.error.login.login.localCache1", name, handlerId);
            return new MessageData("system.error");
        }
        return new MessageData("login.login.success");
    }

    @Cmd("register")
    public MessageData register(String handlerId, String name, String pwd){
        UserCacheBean bean = userDao.findUserByName(name);
        if (bean != null){
            return new MessageData("login.register.fail.nameExist");
        }
        bean = new UserCacheBean().setUname(name).setPwd(pwd).setGold(0);
        int i = userDao.saveUser(bean);
        if (i == -1){
            LogUtil.error(LoginCmd.class, "log.error.login.register.sql", name);
            return new MessageData("system.error");
        }
        return new MessageData("login.register.success");
    }

    /**用户退出时,如果用户所在房间正在进行游戏且用户不是最后一个人玩家, 则机器人代打
     * 如果用户是最后一个人玩家则结束房间游戏
     * */
    @Cmd(SIGN_OUT)
    public MessageData logout(String handlerId){
        String name = LoginUtil.offLine(handlerId);
        if (name == null){//未登录
            return null;
        }
        //已登录
        MessageData[] messageData = roomModel.userExit(name);
        UserCacheBean bean = userCache.findUserCacheBeanByName(name);
        if (messageData != null){
            long decr = bean.getGold() - 128;//惩罚分机制
            bean.setGold(decr);
        }
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            int i = userDao.updateUser(bean);
            if (i == -1){
                LogUtil.error(LoginCmd.class, "log.error.login.logout.sql", name);
                throw new RuntimeException("log.error.login.logout.sql");
            }
            long l = userCache.delUserCacheBeanByName(name);
            if (l == 0) {
                LogUtil.error(LoginCmd.class, "log.error.login.logout.redis", name);
            }
        });
        return messageData != null ? new MessageData().setOthers(messageData) : null;
    }
}
