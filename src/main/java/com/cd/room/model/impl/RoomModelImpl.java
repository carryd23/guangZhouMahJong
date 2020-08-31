package com.cd.room.model.impl;

import com.cd.core.data.MessageData;
import com.cd.room.cache.RoomLocalCache;
import com.cd.room.cache.impl.RoomLocalCacheImpl;
import com.cd.room.model.RoomModel;
import com.cd.room.pojo.*;
import com.cd.room.utils.MahJongUtil;
import com.cd.room.utils.RobotHelperUtil;
import com.cd.room.utils.RoomUtil;
import com.cd.room.utils.SortUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.cd.core.utils.ConstantUtil.DEF_SPEAKER;

public class RoomModelImpl implements RoomModel {
    private static RoomLocalCache roomLocalCache = RoomLocalCacheImpl.newInstance();

    @Override
    public MessageData[] userExit(String name){
        if (!roomLocalCache.inRoom(name)){
            return null;
        }
        Room room = roomLocalCache.findRoomByName(name);
        roomLocalCache.removeName(name);
        Player player = room.getPlayerByName(name);
        if (room.getOwner().equals(name)){//如果是房主
            boolean b = room.changeOwner();
            if (!b){//该房间没有人玩家了
                if (room.getStarted()){
                    room.setStarted(false);//设置房间状态,让其他的机器人停下来
                }
                room.remove(name);
                roomLocalCache.removeRoomById(room.getId());//关闭该room
                return null;
            }
        }
        if (room.getStarted()){//如果房间正在进行游戏
            player.setIsRobot(true);
            if (room.getActionPerformer().equals(name)){//防止该用户未完成动作时掉线
                return RobotHelperUtil.robotActionDispatcher(room, player);
            }
        }else {
            room.remove(name);
        }
        return null;
    }

    @Override
    public List<MessageData> startGame(Room room) {
        //房间游戏初始化, 包括获取麻将、掷骰子
        room.roomInit();
        //发牌
        //从庄家开始发牌
        Player player = room.getBanker();
        for (int i = 0; i < 3; i++){//发3圈
            for (int j = 0; j < 4; j++){
                List<MahJong> licensing = room.licensing(4);//发4张牌
                assert licensing.size() == 4;
                player.getHand().addAll(licensing);//保存至玩家的手牌中
                player = room.nextPlayer(player);//获取下一个player
                assert player != null;
            }
        }
        for (int i = 0; i < 4; i++){//发1圈
            MahJong licensing = room.licensingHead();//发1张牌
            player.getHand().add(licensing);//保存至玩家的手牌中
            player = room.nextPlayer(player);//获取下一个player
            assert player != null;
        }
        player.getHand().add(room.licensingHead());//庄家多发一张牌
        for (int i = 0; i < 4; i++){
            SortUtil.insertSort(player.getHand());
            player = room.nextPlayer(player);
            assert player != null;
        }
        //设置庄家出牌任务
        room.setActionTask(player.getName(), ActionTask.TaskType.PLAY);
        List<MessageData> messageDataList = new ArrayList<>();
        boolean hu = false, anGang = false;
        if (player.canHu()){//如果是天胡
            hu = true;
            room.addActionTask(ActionTask.TaskType.HU);
        }
        List<Integer> indexs = player.canAnGang();
        if (indexs != null){//刚开局, 只能暗杠
            anGang = true;
            room.addActionTask(ActionTask.TaskType.AN_GANG);
        }
        if (player.isRobot()){//如果庄家是机器人,自动出牌
            MessageData[] messageData = RobotHelperUtil.robotActionDispatcher(room, player);
            messageDataList.addAll(Arrays.asList(messageData));
        }else {
            messageDataList.add(new MessageData(player.getName(), "game.play.tip"));
            if (hu){
                messageDataList.add(new MessageData(player.getName(), "game.huPai.tip"));
            }
            if (anGang){
                for (Integer index : indexs){
                    messageDataList.add(new MessageData(player.getName(), "game.gangPai.anGang.tip", index + 1));
                }
            }
        }
        return messageDataList;
    }

    @Override
    public List<MessageData> readyToPlay(Room room, Player player) {
        MahJong mahJong = room.licensingHead();//发出一张牌
        player.draw(mahJong);//抓一张牌
        return preparePlay(room, player, mahJong);
    }

    @Override
    public List<MessageData> afterGangPai(Room room, Player player) {
        if (room.mahJongRunOut()){//如果没有麻将则游戏结束
            endTheGame(room, room.getBanker());
            MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.end.tip");
            return Arrays.asList(messageData);
        }
        MahJong mahJong = room.licensingEnd();//从末尾发一张牌
        player.draw(mahJong);//抓一张牌
        return preparePlay(room, player, mahJong);
    }

    private List<MessageData> preparePlay(Room room, Player player, MahJong draw){
        String name = player.getName();
        room.setActionTask(name, ActionTask.TaskType.PLAY);
        boolean canHu = false, bGang = false, aGang = false;
        if (player.canHu()){
            canHu = true;
            room.addActionTask(ActionTask.TaskType.HU);
        }
        List<Integer> baGang = player.canBaGang();
        if (baGang != null){//检查是否可巴杠刚
            bGang = true;
            room.addActionTask(ActionTask.TaskType.BA_GANG);
        }
        List<Integer> anGang = player.canAnGang();
        if (anGang != null){
            aGang = true;
            room.addActionTask(ActionTask.TaskType.AN_GANG);
        }
        if (player.isRobot()){
            MessageData[] messageData = RobotHelperUtil.robotActionDispatcher(room, player);
            return Arrays.asList(messageData);
        }else {
            List<MessageData> list = new ArrayList<>();
            list.add(new MessageData(player.getName(), "game.msg.mahjongs", MahJongUtil.getPlayHandIndexs(player.getHand())));
            list.add(new MessageData(name, "game.play.tip.nextPlayer", draw));
            if (canHu){
                list.add(new MessageData(name, "game.huPai.tip"));
            }
            if (aGang){
                for (Integer index : anGang){
                    list.add(new MessageData(name, "game.gangPai.anGang.tip", index + 1, index + 1));
                }
            }
            if (bGang){
                for (Integer index : baGang){
                    list.add(new MessageData(name, "game.gangPai.baGang.tip", index + 1, index + 1));
                }
            }
            return list;
        }
    }

    @Override
    public MessageData[] playCard(Room room, Player player, int index) {
        String name = player.getName();
        room.setWhoseTurn(name);//设置出牌人
        MahJong play = player.play(index);//出的牌
        room.setLastCard(play);//保存这张牌
        MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.play.sucess.msg", name, play);
        MessageData[] daces;
        Player canPengPlayer = MahJongUtil.findCanPeng(room, name, play);//遍历玩家, 看是否有玩家能碰这张牌
        if (canPengPlayer == null){//如果没人能够peng, 则设置下家出牌任务
            if (room.mahJongRunOut()){//没有麻将,
                endTheGame(room, room.getBanker());// 流局, 游戏结束. 庄家不变
                daces = RoomUtil.roomChat(room, DEF_SPEAKER, "game.end.tip");
                return RoomUtil.addAllMessageData(messageData, daces);
            }else {
                canPengPlayer = room.getWhoseTurn();//获取下一个出牌人
                List<MessageData> list = readyToPlay(room, canPengPlayer);
                daces = RoomUtil.addAllMessageData(list, messageData);
                return daces;
            }
        }else {//如果有人能够peng
            room.setActionTask(canPengPlayer.getName(), ActionTask.TaskType.PENG);//设置为能碰状态
            boolean mGang = false;
            if (canPengPlayer.canMingGang(play)){//如果还能杠这张牌
                mGang = true;
                room.addActionTask(ActionTask.TaskType.GANG);//设置为能杠状态
            }
            if (canPengPlayer.isRobot()){//如果是机器人, 自动杠或碰
                daces = RobotHelperUtil.robotActionDispatcher(room, canPengPlayer);
                return RoomUtil.addAllMessageData(messageData, daces);
            }else {//回显命令提示给用户
                List<MessageData> messageList = new ArrayList<>();
                messageList.add(new MessageData(canPengPlayer.getName(), "game.msg.mahjongs", MahJongUtil.getPlayHandIndexs(canPengPlayer.getHand())));
                messageList.add(new MessageData(canPengPlayer.getName(), "game.pengPai.tip", play));
                if (mGang){
                    messageList.add(new MessageData(canPengPlayer.getName(), "game.gangPai.tip", play));
                }
                return RoomUtil.addAllMessageData(messageList, messageData);
            }
        }
    }

    @Override
    public void endTheGame(Room room, Player player) {
        room.setStarted(false);
        room.cleanAllActionTask();
        if (!room.getBanker().getName().equals(player.getName())){
            room.setBanker(player.getName());
        }

        for (int i = 0; i < room.getTotalOfPlayers() ; i++){
            player.getHand().clear();
            player.getTouchCards().clear();
            player.getAnGang().clear();
            player.getMingGang().clear();
            Set<Integer> jiangCardSet = player.getJiangCardSet();
            if (jiangCardSet != null){
                jiangCardSet.clear();
            }
            player = room.nextPlayer(player);
        }
    }

    @Override
    public HuPai huPai(Player player) {
        HuPai[] values = HuPai.values();
        for (int i = values.length - 1; i >= 0; i--){
            HuPai value = values[i];
            if (value.eligible(player)){
                return value;
            }
        }
        return null;
    }
}
