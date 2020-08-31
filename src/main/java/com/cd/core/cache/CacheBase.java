package com.cd.core.cache;

import com.cd.core.utils.ConvertUtil;
import com.cd.core.utils.RedisUtil;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class CacheBase {

    public String saveObject(String key, Object obj){
        Map<String, String> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String val = null;
            try {
                val = String.valueOf(field.get(obj));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            map.put(name, val);
        }
        try(Jedis jedis = RedisUtil.getJedis()){
            return jedis.hmset(key, map);
        }
    }

    public <T> T getObject(Class<T> cla, String key){
        Map<String, String> map;
        try(Jedis jedis = RedisUtil.getJedis()){
            map = jedis.hgetAll(key);
        }
        T obj;
        try {
            obj = cla.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, String> entry : map.entrySet()){
            try {
                Field field = cla.getDeclaredField(entry.getKey());
                ConvertUtil.convertField(obj, field, entry.getValue());
            }catch (NoSuchFieldException e){
                throw new RuntimeException(MessageFormat.format("class {0} have no field named {1}", obj.getClass().getName(), entry.getKey()), e);
            }
        }
        return obj;
    }

    public long delKey(String key){
        try(Jedis jedis = RedisUtil.getJedis()){
            return jedis.del(key);
        }
    }
}
