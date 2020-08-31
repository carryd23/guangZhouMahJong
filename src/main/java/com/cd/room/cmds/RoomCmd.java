package com.cd.room.cmds;

import com.cd.core.anno.Cmd;
import com.cd.core.data.MessageData;
import com.cd.core.log.LogUtil;
import com.cd.room.cache.RoomLocalCache;
import com.cd.room.cache.impl.RoomLocalCacheImpl;
import com.cd.room.pojo.Room;
import com.cd.room.utils.RoomUtil;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.cd.core.utils.ConstantUtil.DEF_SPEAKER;

@Cmd("room")
public class RoomCmd {
    private RoomLocalCache localCache = RoomLocalCacheImpl.newInstance();
    /**
     * 创建房间
     * @param name
     * @return
     */
    @Cmd("create")
    public MessageData createRoom(String name){
        boolean repeat = localCache.inRoom(name);
        if (repeat){
            return new MessageData("room.create.fail.nameRepeat");
        }
        Room room = new Room(name);
        boolean b = localCache.addRoom(room);
        if (!b){
            LogUtil.error(RoomCmd.class, "log.error.room.create.msg1", room.getOwner(), room.getId());
            return new MessageData("system.error");
        }
        return new MessageData("room.create.success");
    }

    /**
     * 加入房间
     * @param name
     * @param rid
     * @return
     */
    @Cmd("join")
    public MessageData joinRoom(String name, int rid){
        boolean b = localCache.inRoom(name);
        if (b){
            return new MessageData("room.join.fail.inOtherRoom").setArgs(localCache.findRoomByName(name).getId(), rid);
        }
        Room room = localCache.findRoomById(rid);
        if (room == null){
            return new MessageData("room.join.fail.idNotExist").setArgs(rid);
        }
        if (room.getStarted()){
            return new MessageData("room.join.fail.roomStarted");
        }
        boolean add = room.add(name);
        if (!add){
            return new MessageData("room.join.fail.roomFull");
        }
        localCache.addName2Room(name, room);
        MessageData data = new MessageData(name,"room.join.success", rid);
        MessageData[] othersData = RoomUtil.roomChat(room, DEF_SPEAKER, "room.join.success.otherMsg", name);
        data.setOthers(othersData);
        return data;
    }

    /**
     * 退出房间
     * @param name
     * @return
     */
    @Cmd("exit")
    public MessageData exitRoom(String name){
        boolean inTheRoom = localCache.inRoom(name);
        if (!inTheRoom){
            return new MessageData("room.exit.fail.notInRoom");
        }
        /**删除该用户的room记录, localCache记录
         * */
        Room room = localCache.findRoomByName(name);
        if (room.getStarted()){
            return new MessageData("room.exit.fail.gameStarted");
        }
        localCache.removeName(name);
        /**如果该房间还有人
         *  1.如果退出的是房主， 更换房主
         * 如果房间没人了, 则关闭该房间
         * */
        MessageData self = new MessageData("room.exit.success");
        MessageData changeOwner = null;
        if (room.getOwner().equals(name)){
            boolean b = room.changeOwner();
            if (b){
                changeOwner = new MessageData(room.getOwner(), "room.exit.otherMsg.owner");
            }else {//更换房主失败, 证明该房间内已经没有非机器人玩家
                localCache.removeRoomById(room.getId());//关闭该房间
                return self;
            }
        }
        room.remove(name);//从房间中删除该用户
        MessageData[] othersData = RoomUtil.roomChat(room, DEF_SPEAKER, "room.exit.otherMsg", name);
        if (changeOwner == null){
            return self.setOthers(othersData);
        }
        int length = othersData.length;
        MessageData[] allMsg = new MessageData[1 + length];
        System.arraycopy(othersData, 0, allMsg, 0, length);
        allMsg[length] = changeOwner;
        return self.setOthers(allMsg);
    }

    @Cmd("list")
    public MessageData roomList(String name){
        List<MessageData> list = new ArrayList<>();
        Enumeration<Integer> rids = localCache.getAllRoom();
        while (rids.hasMoreElements()){
            Integer rid = rids.nextElement();
            Room room = localCache.findRoomById(rid);
            list.add(new MessageData(name, "room.list.showMsg", rid, room.getRemainingSeat()));
        }
        if (list.size() == 0){
            return new MessageData("room.list.nothing");
        }
        MessageData[] others = new MessageData[list.size()];
        return new MessageData().setOthers(list.toArray(others));
    }

    @Cmd("chat")
    public MessageData chat(String name, String[] params){
        boolean in = localCache.inRoom(name);
        if (!in){
            return new MessageData("room.error");
        }
        if(params.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
            sb.append(param);
            sb.append(" ");
        }
        //删除最后一个空格
        sb.deleteCharAt(sb.length()-1);
        Room room = localCache.findRoomByName(name);
        MessageData[] messageData = RoomUtil.roomChat(room, name,"room.chat.success", name, sb.toString());
        return new MessageData().setOthers(messageData);
    }
}
