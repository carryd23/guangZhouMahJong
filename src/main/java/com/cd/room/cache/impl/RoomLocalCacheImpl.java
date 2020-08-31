package com.cd.room.cache.impl;


import com.cd.room.cache.RoomLocalCache;
import com.cd.room.pojo.Room;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class RoomLocalCacheImpl implements RoomLocalCache {
    private ConcurrentHashMap<String, Room> name2Room = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, Room> id2Room = new ConcurrentHashMap<>();

    private static volatile RoomLocalCacheImpl localCache;
    private RoomLocalCacheImpl(){}

    public static RoomLocalCacheImpl newInstance(){
        if (localCache == null){
            synchronized (RoomLocalCacheImpl.class){
                if (localCache == null){
                    localCache = new RoomLocalCacheImpl();
                }
            }
        }
        return localCache;
    }

    @Override
    public Room findRoomByName(String name) {
        return name2Room.get(name);
    }

    @Override
    public void removeRoomById(int id) {
        id2Room.remove(id);
    }

    @Override
    public boolean inRoom(String name) {
        return name2Room.containsKey(name);
    }

    @Override
    public boolean addRoom(Room room) {
        Room room1 = name2Room.putIfAbsent(room.getOwner(), room);
        Room room2 = id2Room.putIfAbsent(room.getId(), room);
        return room1 == null && room2 == null;
    }

    @Override
    public void removeName(String name) {
        name2Room.remove(name);
    }

    @Override
    public Room findRoomById(int id) {
        return id2Room.get(id);
    }

    @Override
    public boolean addName2Room(String name, Room room) {
        Room room1 = name2Room.putIfAbsent(name, room);
        return room1 == null;
    }

    @Override
    public Enumeration<Integer> getAllRoom() {
        return id2Room.keys();
    }
}
