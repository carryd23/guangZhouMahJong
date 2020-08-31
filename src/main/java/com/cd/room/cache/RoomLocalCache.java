package com.cd.room.cache;


import com.cd.room.pojo.Room;

import java.util.Enumeration;
import java.util.List;

public interface RoomLocalCache {
    Room findRoomByName(String name);

    void removeRoomById(int id);

    boolean inRoom(String name);

    boolean addRoom(Room room);

    void removeName(String name);

    Room findRoomById(int id);

    boolean addName2Room(String name, Room room);

    Enumeration<Integer> getAllRoom();
}
