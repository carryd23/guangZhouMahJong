package com.cd;


import com.cd.core.log.LogUtil;
import com.cd.core.utils.TipsUtil;
import com.cd.core.vertx.InitializerVerticle;
import com.cd.core.vertx.ServerVerticle;
import io.vertx.core.Vertx;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(InitializerVerticle.class.getName(), res -> {
            if(res.failed()) {
                LogUtil.error(App.class, "log.error.app.msg1", res.cause());
                return;
            }
            vertx.deployVerticle(ServerVerticle.class.getName(), handler -> {
                if(handler.failed()) {
                    LogUtil.error(App.class, "log.error.app.msg2", handler.cause());
                }
            });
        });
    }
}
