package com.cd.room.model;

import com.cd.core.data.MessageData;
import com.cd.room.pojo.HuPai;
import com.cd.room.pojo.Player;
import com.cd.room.pojo.Room;

import java.util.List;

public interface RoomModel {
    /**用户强制下线后执行的操作
     * */
    MessageData[] userExit(String name);

    /**开始游戏
     * */
    List<MessageData> startGame(Room room);

    /**准备出牌
     * */
    List<MessageData> readyToPlay(Room room, Player player);

    /**杠后出牌
     * */
    List<MessageData> afterGangPai(Room room, Player player);

    /**出牌
     * */
    MessageData[] playCard(Room room, Player player, int index);

    /**结束游戏
     * */
    void endTheGame(Room room, Player player);

    /**获取该玩家胡牌的类型
     * */
    HuPai huPai(Player player);
}
