package com.cd.room.cmds;

import com.cd.core.anno.Cmd;
import com.cd.core.data.MessageData;
import com.cd.core.log.LogUtil;
import com.cd.room.cache.MahJongCache;
import com.cd.room.cache.RoomLocalCache;
import com.cd.room.cache.impl.MahJongCacheImpl;
import com.cd.room.cache.impl.RoomLocalCacheImpl;
import com.cd.room.model.RoomModel;
import com.cd.room.model.impl.RoomModelImpl;
import com.cd.room.pojo.*;
import com.cd.room.utils.MahJongUtil;
import com.cd.room.utils.RoomUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.cd.core.utils.ConstantUtil.DEF_SPEAKER;


@Cmd("game")
public class GameCmd {
    private RoomLocalCache roomLocalCache = RoomLocalCacheImpl.newInstance();
    private MahJongCache mahJongCache = new MahJongCacheImpl();
    private RoomModel roomModel = new RoomModelImpl();

    @Cmd("start")
    public MessageData startGame(String name){
        if (!roomLocalCache.inRoom(name)){
            return new MessageData("room.error");
        }
        Room room = roomLocalCache.findRoomByName(name);
        if (room.getStarted()){
            return new MessageData("game.start.fail.started");
        }
        if (!room.getOwner().equals(name)){//不是房主
            return new MessageData("game.start.fail.noOwner");
        }
        room.setStarted(true);//将房间表示已开始状态, 不能让其他人加入

        MessageData[] startTip = RoomUtil.roomChat(room, DEF_SPEAKER, "game.start.tip", name);
        MessageData[] hasRobotTip = null;
        //获取空位添加机器人
        int seat = room.getRemainingSeat();
        if (seat - room.getRobotCount() != 0){//如果有空座位,添加机器人.如果空位上已经有了机器人则不添加
            for (int i = 0; i < seat; i++){
                boolean add = room.addRobot();
                if (!add){
                    LogUtil.error(GameCmd.class, "log.error.game.start.msg1", room.getId());
                    throw new IllegalStateException(MessageFormat.format("put robot in room {0,number,#} error", room.getId()));
                }
            }
        }
        if (seat > 0){
            hasRobotTip = RoomUtil.roomChat(room, DEF_SPEAKER, "game.start.hasRobot.tip",seat);
        }
        List<MessageData> dataList = roomModel.startGame(room);//开始游戏并返回提示信息
        //给所有非机器人玩家回显手牌信息
        List<Player> players = room.getAllNonRobotPlayer();
        MessageData[] othersData;
        if (hasRobotTip == null){
            othersData = new MessageData[startTip.length + players.size() + dataList.size()];
        }else {
            othersData = new MessageData[hasRobotTip.length + startTip.length + players.size() + dataList.size()];
        }
        System.arraycopy(startTip, 0, othersData, 0, startTip.length);
        int index = startTip.length;
        if (hasRobotTip != null){
            System.arraycopy(hasRobotTip, 0, othersData, startTip.length, hasRobotTip.length);
            index += hasRobotTip.length;
        }
        for (Player player : players){
            othersData[index++] = new MessageData(player.getName(), "game.msg.mahjongs", MahJongUtil.getPlayHandIndexs(player.getHand()));
        }
        for (MessageData messageData : dataList){
            othersData[index++] = messageData;
        }
        return new MessageData().setOthers(othersData);
    }

    @Cmd("play")
    public MessageData playCard(String name, int index){
        MessageData data = actionValid(name, ActionTask.TaskType.PLAY);
        if (data != null){
            return data;
        }
        Room room = roomLocalCache.findRoomByName(name);
        Player player = room.getPlayerByName(name);
        //出牌,从手牌中删除指定下标的牌
        if (player.getHand().size() < index){
            return new MessageData(name,"game.play.fail.outOfBound", index);
        }
        MessageData[] messageData = roomModel.playCard(room, player, index - 1);
        return new MessageData().setOthers(messageData);
    }

    @Cmd("peng")
    public MessageData pengMahJong(String name){
        MessageData data = actionValid(name, ActionTask.TaskType.PENG);
        if (data != null){
            return data;
        }
        Room room = roomLocalCache.findRoomByName(name);
        MahJong lastCard = room.getLastCard();
        Player player = room.getPlayerByName(name);
        player.touchCard(lastCard);//执行碰牌逻辑
        //碰完牌之后需要出一张牌
        room.setActionTask(name, ActionTask.TaskType.PLAY);
        return new MessageData(name, "game.peng.success", lastCard).setOthers(
                new MessageData[]{new MessageData(name, "game.msg.mahjongs", MahJongUtil.getPlayHandIndexs(player.getHand()))});
    }

    @Cmd("mgang")
    public MessageData mGangMahJong(String name){//明杠
        MessageData messageData = actionValid(name, ActionTask.TaskType.GANG);
        if (messageData != null){
            return messageData;
        }
        Room room = roomLocalCache.findRoomByName(name);
        Player player = room.getPlayerByName(name);
        player.mingGang(room.getLastCard());//执行暗杠动作
        //明杠完后杠完之后需要出一张牌
        List<MessageData> messageDataList = roomModel.afterGangPai(room, player);//杠牌后逻辑
        return new MessageData().setOthers(RoomUtil.convert2MessageDataArray(messageDataList));
    }

    @Cmd("gang")
    public MessageData gangMahJong(String name, int index){//巴杠, 暗杠
        MessageData messageData = actionValid(name, ActionTask.TaskType.BA_GANG, ActionTask.TaskType.AN_GANG);
        if (messageData != null){
            return messageData;
        }
        Room room = roomLocalCache.findRoomByName(name);
        List<ActionTask.TaskType> types = room.getActionTaskType();
        Player player = room.getPlayerByName(name);
        boolean baGang = false, anGang = false;
        if (types.contains(ActionTask.TaskType.BA_GANG)){
            List<Integer> gang = player.canBaGang();
            if (gang.contains(index - 1)){//代表该命令确实是补明杠
                baGang = true;
                player.baGang(index - 1);
            }
        }else if (types.contains(ActionTask.TaskType.AN_GANG)){//如果不是补明杠, 则应该是暗杠
            List<Integer> gang = player.canAnGang();
            if (gang.contains(index - 1)){
                anGang = true;
                player.anGang(index - 1);
            }
        }
        if (!baGang && !anGang){//表示都没有完成, 则应该是index错误
            return new MessageData(name, "game.gang.fail.index", index);
        }
        List<MessageData> messageDataList = roomModel.afterGangPai(room, player);//杠牌后逻辑
        return new MessageData().setOthers(RoomUtil.convert2MessageDataArray(messageDataList));
    }

    //一般碰牌或者明杠才会执行该命令,表示用户不碰或不明杠或都不, 则需要将出牌人设置为上一个出牌人的下家
    @Cmd("undo")
    public MessageData undo(String name){
        MessageData data = actionValid(name, ActionTask.TaskType.PENG, ActionTask.TaskType.GANG);
        if (data != null){
            return data;
        }
        Room room = roomLocalCache.findRoomByName(name);
        if (room.mahJongRunOut()){//如果没有麻将则游戏结束
            roomModel.endTheGame(room, room.getBanker());
            MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.end.tip");
            return new MessageData().setOthers(messageData);
        }
        //否则应该给下家抓一张牌, 然后再将出牌权给下家
        Player player = room.getWhoseTurn();//可以出牌的玩家
        List<MessageData> list = roomModel.readyToPlay(room, player);
        MessageData[] messageData = RoomUtil.convert2MessageDataArray(list);
        return new MessageData().setOthers(messageData);
    }

    @Cmd("hupai")
    public MessageData huPai(String name){
        MessageData data = actionValid(name, ActionTask.TaskType.HU);
        if (data != null){
            return data;
        }
        Room room = roomLocalCache.findRoomByName(name);
        Player player = room.getPlayerByName(name);
        HuPai pai = roomModel.huPai(player);
        assert pai != null;//至少应该是鸡胡, 绝对不会是null
        roomModel.endTheGame(room, player);//结束房间游戏
        //算番
        List<Player> players = room.getAllNonRobotPlayer();
        int num = (players.size() - 1) * pai.getScore();//减一除去自己
        for (Player otherPlayer : players){
            if (otherPlayer.getName().equals(name)){
                mahJongCache.modifyScore(name, num);
            }else {
                mahJongCache.modifyScore(otherPlayer.getName(), -pai.getScore());
            }
        }
        MessageData[] messageData = RoomUtil.roomChat(room, name, "game.hupai.success.otherMsg", name, pai, pai.getScore());
        return new MessageData(name, "game.hupai.success").setArgs(pai, num).setOthers(messageData);
    }

    @Cmd("view")
    public MessageData view(String name){
        if (!roomLocalCache.inRoom(name)){//没有在房间内
            return new MessageData("room.error");
        }
        Room room = roomLocalCache.findRoomByName(name);
        if (!room.getStarted()){//游戏未开始
            return new MessageData("game.start.fail.alreadyStarted");
        }
        List<MessageData> list = new ArrayList<>();
        //查看房间剩余牌数
        int jong = room.getRemainingMahJong();
        list.add(new MessageData(name, "game.view.room.remainingMahJong", jong));
        //查看自己碰、杠的牌
        Player player = room.getPlayerByName(name);
        Set<MahJong> cards = player.getTouchCards();
        if (!cards.isEmpty()){
            list.add(new MessageData(name, "game.view.self.touch", cards));
        }
        Set<MahJong> mingGang = player.getMingGang();
        if (!mingGang.isEmpty()){
            list.add(new MessageData(name, "game.view.self.mgang", mingGang));
        }
        Set<MahJong> anGang = player.getAnGang();
        if (!anGang.isEmpty()){
            list.add(new MessageData(name, "game.view.self.angang", anGang));
        }
        //查看其他玩家碰、杠的牌
        player = room.nextPlayer(player);
        for (int i = 0; i < room.getTotalOfPlayers() - 2; i++){//减2除去自己和下家
            cards = player.getTouchCards();
            if (cards.size() != 0){
                list.add(new MessageData(name, "game.view.other.touch", player.getName(), cards));
            }
            mingGang = player.getMingGang();
            if (!mingGang.isEmpty()){
                list.add(new MessageData(name, "game.view.other.mgang", player.getName(), mingGang));
            }
            anGang = player.getAnGang();
            if (!anGang.isEmpty()){
                list.add(new MessageData(name, "game.view.other.angang", player.getName(), anGang));
            }
            player = room.nextPlayer(player);
        }
        return new MessageData().setOthers(RoomUtil.addAllMessageData(list));
    }

    private MessageData actionValid(String name, ActionTask.TaskType... types){
        if (!roomLocalCache.inRoom(name)){//没有在房间内
            return new MessageData("room.error");
        }
        Room room = roomLocalCache.findRoomByName(name);
        if (!room.getStarted()){//游戏未开始
            return new MessageData("game.start.fail.alreadyStarted");
        }
        String performer = room.getActionPerformer();
        if (!performer.equals(name)){//与指定的动作执行者不符
            return new MessageData("game.action.fail");
        }
        List<ActionTask.TaskType> taskTypes = room.getActionTaskType();//该用户可以完成的任务是否包含出牌
        int count = 0;
        for (ActionTask.TaskType type : types){
            if (taskTypes.contains(type)){
                count++;
            }
        }
        return count == 0 ? new MessageData("game.action.fail.notAllow") : null;
    }


}
