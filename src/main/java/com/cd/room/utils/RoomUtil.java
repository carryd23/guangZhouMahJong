package com.cd.room.utils;

import com.cd.core.data.MessageData;
import com.cd.room.pojo.Player;
import com.cd.room.pojo.Room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class RoomUtil {
    public static MessageData[] roomChat(Room room, String speaker,  String msg, Object...args){//String speaker,
        List<MessageData> others = new ArrayList<>();
        List<Player> list = room.getAllNonRobotPlayer();
        for (Player player : list){
            if (player.getName().equals(speaker)){//跳过发言者
                continue;
            }
            others.add(new MessageData(player.getName(), msg, args));
        }
        MessageData[] othersData = new MessageData[others.size()];
        return others.toArray(othersData);
    }

    public static MessageData[] convert2MessageDataArray(Collection<MessageData> collection){
        return addAllMessageData(collection);
    }

    public static MessageData[] addAllMessageData(Collection<MessageData> collection, MessageData...datas){
        MessageData[] messageData = new MessageData[collection.size() + datas.length];
        int index = 0;
        for (MessageData data : datas){
            messageData[index++] = data;
        }
        for (MessageData data : collection){
            messageData[index++] = data;
        }
        return messageData;
    }

    public static MessageData[] addAllMessageData(MessageData[] arr1, MessageData[] arr2){
        MessageData[] all = new MessageData[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, all, 0, arr1.length);
        System.arraycopy(arr2, 0, all, arr1.length, arr2.length);
        return all;
    }
}
