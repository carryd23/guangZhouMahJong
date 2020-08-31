package com.cd.room.cache.impl;

import com.cd.room.cache.MahJongLocalCache;
import com.cd.room.pojo.MahJong;
import com.cd.room.pojo.MahJongType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MahJongLocalCacheImpl implements MahJongLocalCache {
    private static List<MahJong> mahJongs;
    private static volatile MahJongLocalCacheImpl localCache;

    public static MahJongLocalCacheImpl newInstance(){
        if (localCache == null){
            synchronized (MahJongLocalCacheImpl.class){
                if (localCache == null){
                    localCache = new MahJongLocalCacheImpl();
                    mahJongs = new ArrayList<>(136);
                    MahJongType[] values = MahJongType.values();
                    for (MahJongType type : values){
                        int totalType = type.getTotalType();
                        if (totalType > 1){
                            for (int i = 1 ; i <= totalType; i++){
                                for (int j = 0; j < 4; j++){
                                    mahJongs.add(new MahJong(i, type));
                                }
                            }
                        }else {
                            for (int j = 0; j < 4; j++){
                                mahJongs.add(new MahJong(type));
                            }
                        }
                    }
                }
            }
        }
        return localCache;
    }

    //将麻将分为4堆, 每一堆都有34张牌
    @Override
    public List<List<MahJong>> getMahJong() {
        List<MahJong> total = new ArrayList<>(136);
        total.addAll(mahJongs);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Collections.shuffle(total, random);
        List<List<MahJong>> arr = new ArrayList<>(4);
        int freq = -1;
        for (int i = 0; i < total.size(); i++){
            if (i % 34 == 0){
                freq++;
                arr.add(new ArrayList<>(34));
            }
            arr.get(freq).add(total.get(i));
        }
        return arr;
    }
}
