package com.cd.core.cache;

public interface CoreLocalCache {
    String getNameById(String id);
    String getIdByName(String name);
    boolean putIfAbsentNameAndId(String id, String name);
    boolean containsId(String id);
    boolean containsName(String name);
    String removeId(String id);
    String removeName(String name);

}
