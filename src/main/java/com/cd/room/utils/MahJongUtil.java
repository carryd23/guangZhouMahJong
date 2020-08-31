package com.cd.room.utils;

import com.cd.core.data.MessageData;
import com.cd.room.pojo.*;

import java.util.*;

import static com.cd.core.utils.ConstantUtil.DEF_SPEAKER;

public class MahJongUtil {



    /**指定的麻将list中, 有一种作为将牌，其他的都是刻子
     * */
    public static boolean delJiangAndFindTotalKeZi(Player player, List<MahJong> list){
        Set<Integer> jiangCardSet = player.getJiangCardSet();
        Integer next = jiangCardSet.iterator().next();
        List<MahJong> hand = player.getHand();
        MahJong mahJong = hand.get(next);
        if (!list.contains(mahJong)){//如果将牌不是指定list中的牌
            return false;
        }
        list.remove(mahJong);//删除list中作为将的牌
        int total = MahJongUtil.findTotalOfKeZi(player, list);//寻找list中其他牌的刻子数
        return list.size() == total;//判断其他牌组成的刻子是否完全
    }


    /**
     * 根据需要的刻子类型, 寻找用户的牌中符合的刻子总数
     * @param player 用户
     * @param needs 需要的刻子类型
     * @return
     */
    public static int findTotalOfKeZi(Player player, List<MahJong> needs){
        List<MahJong> hand = player.getHand();
        assert SortUtil.isSorted(hand);
        Iterator<Integer> iterator = player.getJiangCardSet().iterator();
        Integer next = iterator.next();
        if (needs.contains(hand.get(next))){//如果用做刻子的牌做将,则不满足
            return -1;
        }
        Set<MahJong> touchCards = player.getTouchCards();
        Set<MahJong> mingGang = player.getMingGang();
        Set<MahJong> anGang = player.getAnGang();
        int count = 0, sum;//找到的做刻子的牌刻子总数
        for (MahJong mahJong : needs){//从碰、杠set中寻找做刻子的牌刻子
            if (touchCards.contains(mahJong) || mingGang.contains(mahJong) || anGang.contains(mahJong)){
                count++;
            }else {
                sum = findTotalOfMahJong(hand, mahJong);
                if (sum == 3){//3个为刻子
                    count++;
                }else {//在三个set中都没有的情况下, hand 中再没有则不满足
                    return -1;//不满足刻子牌要求3个
                }
            }
        }
        return count;
    }

    public static int findTotalOfMahJong(List<MahJong> hand, MahJong mahJong){
        assert SortUtil.isSorted(hand);
        int search = Collections.binarySearch(hand, mahJong);
        if (search < 0){//找不到直接return 0
            return 0;
        }
        int sum = 1;//找到一个
        //left search
        for (int i = search - 1; i >= 0; i--){
            if (hand.get(i).equals(mahJong)){
                sum++;
                continue;
            }
            break;
        }
        //right search
        for (int i = search + 1; i < hand.size(); i++){
            if (hand.get(i).equals(mahJong)){
                sum++;
                continue;
            }
            break;
        }
        return sum;
    }

    //遍历并判断某个玩家(除上一次出牌的玩家外)是否可以碰 如果是则执行相应动作
    public static Player findCanPeng(Room room, String name, MahJong lastCard){
        int total = room.getTotalOfPlayers();
        boolean canPeng = false;
        Player player = null;
        for (int i = 0; i < total - 1; i++){//减一排除自己
            player = room.nextPlayer(name);
            if (player.canPeng(lastCard)){
                canPeng = true;
                break;
            }
            name = player.getName();
        }
        if (canPeng){
            return player;
        }
        return null;
    }


    /**获取手牌的下标,用于回显
     * */
    public static String getPlayHandIndexs(List<MahJong> hand){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++){
            sb.append(hand.get(i));
            sb.append("(");
            sb.append(i  + 1);
            sb.append(")");
            sb.append(" ");
        }
        if (sb.length() > 0){
            sb.deleteCharAt(sb.length() - 1);//删掉最后一个空格
        }
        return sb.toString();
    }
}
