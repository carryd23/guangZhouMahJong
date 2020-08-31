package com.cd.core.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Properties;

public class TipsUtil {
    private volatile static Properties properties;

    public static void initTips(){
        if (properties == null){
            synchronized (TipsUtil.class) {
                if(properties == null) {
                    properties = new Properties();
                }
            }
            InputStreamReader reader = new InputStreamReader(TipsUtil.class.getClassLoader().getResourceAsStream("tips.properties"));
            try{
                properties.load(reader);
            }catch (IOException e){
                throw new RuntimeException("tips properties error", e);
            }finally {
                try{
                    reader.close();
                }catch (IOException e){
                    throw new RuntimeException("reader close error", e);
                }
            }
        }
    }

    public static String getTip(String key, Object... args){
        String val = properties.getProperty(key);
        if(args.length == 0) {
            return val;
        }
        return MessageFormat.format(val, args);
    }
}
