package com.cd.core.log;

import com.cd.core.utils.TipsUtil;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.text.MessageFormat;

public class LogUtil {
    private static Logger log = LoggerFactory.getLogger(LogUtil.class);

    public static void info(Class<?> cla, String msg, Object...args){
        log.info(MessageFormat.format("class {0} info: {1}", cla.getName(), TipsUtil.getTip(msg, args)));
    }

    public static void error(Class<?> cla, String msg, Object...args){
        log.error(MessageFormat.format("class {0} error: {1}", cla.getName(), TipsUtil.getTip(msg, args)));
    }
}
