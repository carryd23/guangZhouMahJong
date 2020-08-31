package com.cd.core.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RedisUtil {
    private static JedisPool pool;

    public static void initJedisPool(){
        if (pool == null){
            synchronized (RedisUtil.class){
                if (pool == null){
                    Properties properties = new Properties();
                    try {
                        InputStream stream = RedisUtil.class.getClassLoader().getResourceAsStream("redis.properties");
                        properties.load(stream);
                        pool = new JedisPool(properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
                    }catch (IOException e){
                        throw new RuntimeException("read redis properties file error",e);
                    }catch (NumberFormatException e){
                        throw new RuntimeException("redis port must is a Number");
                    }
                }
            }
        }
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }
}
