package com.cd.core.vertx;

import com.cd.core.utils.*;
import io.vertx.core.AbstractVerticle;

public class InitializerVerticle extends AbstractVerticle {
    @Override
    public void start(){
        CmdClassLoader.loadClass();

        JDBCUtil.initDataSource();

        RedisUtil.initJedisPool();

        MessageUtil.initEventBus(vertx.eventBus());

        TipsUtil.initTips();
    }
}
