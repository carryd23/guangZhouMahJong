package com.cd.room.utils;

import com.cd.core.data.MessageData;
import com.cd.room.model.RoomModel;
import com.cd.room.model.impl.RoomModelImpl;
import com.cd.room.pojo.*;

import java.util.*;

import static com.cd.core.utils.ConstantUtil.DEF_SPEAKER;

public class RobotHelperUtil {
    private static RoomModel roomModel = new RoomModelImpl();

    /**机器人6种动作任选一执行
     * */
    public static MessageData[] robotActionDispatcher(Room room, Player robot){
        if (!room.getStarted() || !robot.isRobot()){//如果游戏结束或player不是机器人
            return null;
        }
        List<ActionTask.TaskType> actionTaskType = room.getActionTaskType();
        List<MessageData> messageDataList = new ArrayList<>();
        if (actionTaskType.contains(ActionTask.TaskType.PLAY)){//该机器人出牌阶段才可以可以出现胡、暗杠、补明杠、等可能
            if (actionTaskType.contains(ActionTask.TaskType.HU)){//胡了,不算番
                HuPai pai = roomModel.huPai(robot);
                List<MahJong> hand = new ArrayList<>(robot.getHand());
                MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.robot.hupai", robot.getName(), pai, hand);
                roomModel.endTheGame(room, robot);
                return messageData;
            }else if (actionTaskType.contains(ActionTask.TaskType.AN_GANG)){//暗杠 从末尾抓一张牌, 然后出一张牌
                List<Integer> integers = robot.canAnGang();
                Integer index = integers.get(0);
                MahJong mahJong = robot.getHand().get(index);
                robot.anGang(index);//执行暗杠动作
                MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.gang.robot.angang.tip", robot.getName(), mahJong);
                messageDataList.addAll(roomModel.afterGangPai(room, robot));
                return RoomUtil.addAllMessageData(messageDataList, messageData);
            }else if (actionTaskType.contains(ActionTask.TaskType.BA_GANG)){//补明杠
                List<Integer> integers = robot.canBaGang();
                Integer index = integers.get(0);
                MahJong mahJong = robot.getHand().get(index);
                robot.baGang(index);//执行补明杠动作
                MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.gang.robot.bagang.tip", robot.getName(), mahJong);
                messageDataList.addAll(roomModel.afterGangPai(room, robot));
                return RoomUtil.addAllMessageData(messageDataList, messageData);
            }else {//出牌
                int index = getRobotPlayIndex(robot.getHand());
                MessageData[] messageData = roomModel.playCard(room, robot, index);
                return messageData;
            }
        }else {//未出牌阶段可能有碰、杠等可能，但只能选择一种行为
            MahJong lastCard = room.getLastCard();
            if (actionTaskType.contains(ActionTask.TaskType.GANG)){//杠
                robot.mingGang(lastCard);
                MessageData[] messageData = RoomUtil.roomChat(room, DEF_SPEAKER, "game.gang.robot.mgang.tip", robot.getName(), lastCard);
                messageDataList.addAll(roomModel.afterGangPai(room, robot));
                return RoomUtil.addAllMessageData(messageDataList, messageData);
            }else{//碰,碰完后需要出一张牌
                robot.touchCard(lastCard);
                int index = getRobotPlayIndex(robot.getHand());
                MessageData[] messageData1 = RoomUtil.roomChat(room, DEF_SPEAKER, "game.peng.robot.tip", robot.getName(), lastCard);
                MessageData[] messageData = roomModel.playCard(room, robot, index);
                return RoomUtil.addAllMessageData(messageData1, messageData);
            }
        }
    }

    /**机器人出牌,出最单的牌
     * 如果有3 4 6 三张牌, 则应该出6
     * 如果没有最单的牌 则看对子个数 对子大于1 则拆对 否则拆掉牌面最小的半顺子
     * */
    private static int getRobotPlayIndex(List<MahJong> hand){
        List<MahJong> stack = new ArrayList<>();
        for (MahJong jong : hand) {
            stack.add(jong);
            if (stack.size() >= 3) {
                StackUtil.popKeZi(stack);
                StackUtil.popShunZi(stack);
            }
        }
        Set<Integer> duiZi = new HashSet<>();
        Set<Integer> bShunZi = new HashSet<>();
        //stack最少还剩2个元素
        for (int i = 1; i < stack.size(); i++){
            MahJong left = stack.get(i - 1);
            MahJong right = stack.get(i);
            if (left.equals(right)){//对子
                duiZi.add(i - 1);
            }else if (left.getType() == right.getType() && left.getNum() == right.getNum() - 1){//半顺子
                bShunZi.add(i - 1);
            }
        }
        List<Integer> out = new ArrayList<>();
        //查看现在是否有最单的牌
        for (int i = 0; i < stack.size(); i++){
            if (duiZi.contains(i) || bShunZi.contains(i)){
                i += 1;
                continue;
            }
            out.add(i);
        }
        if (out.isEmpty()){//没有最单的牌
            //有些牌即可被作为半顺子，又可被作为对子
            int index = -1, firstD = -1;
            boolean find = false;
            Iterator<Integer> dui = duiZi.iterator();
            while (dui.hasNext() && !find){
                Integer d = dui.next();
                if (firstD == -1){
                    firstD = d;
                }
                for (Integer next : bShunZi) {
                    if (stack.get(d).equals(stack.get(next))) {
                        index = next;
                        find = true;
                        break;
                    }
                }
            }
            if (duiZi.size() > bShunZi.size()){//如果对子数较多
                if (index != -1){//并且存在复用
                    return Collections.binarySearch(hand, stack.get(index));//删除这个
                }
                //随便拆一个
                return Collections.binarySearch(hand, stack.get(firstD));//删除这个
            }else {
                if (index != -1){
                    return Collections.binarySearch(hand, stack.get(index + 1));//删除这个
                }
                //随便拆一个
                int firstS = bShunZi.iterator().next();
                return Collections.binarySearch(hand, stack.get(firstS));//删除这个
            }
        }else {//有最单的牌, 则应该去掉字牌,因为字牌少比筒索牌少
            int max = 0;
            if (out.size() >= 2){
                for (int i = 1; i < out.size(); i++){
                    MahJong mahJong = stack.get(out.get(i));
                    if (mahJong.compareTo(stack.get(out.get(max))) > 0){//在MahJongType中, 字牌比较大
                        max = i;
                    }
                }
            }
            return Collections.binarySearch(hand, stack.get(out.get(max)));
        }
    }
}
