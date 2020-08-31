package com.cd.room.cache.impl;

import com.cd.core.utils.RedisUtil;
import com.cd.room.cache.MahJongCache;
import redis.clients.jedis.Jedis;

public class MahJongCacheImpl implements MahJongCache {
    @Override
    public void modifyScore(String name, int socre) {
        try(Jedis jedis = RedisUtil.getJedis()){
            jedis.hincrBy(name, "gold", socre);
        }
    }
}
