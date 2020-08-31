package com.cd.room.cache;

import com.cd.room.pojo.MahJong;

import java.util.List;

public interface MahJongLocalCache {
    //获取一副打乱顺序的完整麻将
    List<List<MahJong>> getMahJong();
}
