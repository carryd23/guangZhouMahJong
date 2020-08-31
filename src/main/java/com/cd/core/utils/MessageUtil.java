package com.cd.core.utils;

import com.cd.core.cache.CoreLocalCache;
import com.cd.core.cache.impl.CoreLocalCacheImpl;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import static com.cd.core.utils.ConstantUtil.LINE_SEPARATOR;
import static com.cd.core.utils.ConstantUtil.CHARSET;

public class MessageUtil {
    private volatile static MessageUtil helper;
    private static EventBus eb;
    private CoreLocalCache localCache = CoreLocalCacheImpl.newInstance();

    private MessageUtil() {}

    public static MessageUtil newInstance() {
        if(helper == null) {
            synchronized (MessageUtil.class) {
                if(helper == null) {
                    helper = new MessageUtil();
                }
            }
        }
        return helper;
    }

    public static void initEventBus(EventBus eb){
        if (eb == null){
            throw new IllegalArgumentException("MessageUtil init eventBus must not null");
        }
        MessageUtil.eb = eb;
    }

    public void sendMsg(String name, String msg){
        String hid = localCache.getIdByName(name);
        if (hid == null){
            return;
        }
        eb.send(hid, Buffer.buffer(msg + LINE_SEPARATOR, CHARSET));
    }
}
