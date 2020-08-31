package com.cd.room.pojo;

import com.cd.core.log.LogUtil;
import com.cd.room.utils.MahJongUtil;
import com.cd.room.utils.SortUtil;
import com.cd.room.utils.StackUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Player {
    private String name;//名字
    private List<MahJong> hand;//手牌
    private Set<MahJong> touchCards;//碰的牌
    private Set<MahJong> mingGang;//明杠的牌
    private Set<MahJong> anGang;//暗杠的牌
    private Set<Integer> jiangCardSet;//记录下该玩家胡牌时作为将牌的俩张牌的下标
    private AtomicBoolean isRobot;//是否是机器人

    public Player(String name){
        this.name = name;
        hand = new ArrayList<>();
        touchCards = new HashSet<>();
        mingGang = new HashSet<>();
        anGang = new HashSet<>();
        isRobot = new AtomicBoolean(false);//默认不是
    }

    public boolean isRobot() {
        return isRobot.get();
    }

    public Player setIsRobot(boolean isRobot) {
        this.isRobot.set(isRobot);
        return this;
    }

    public String getName() {
        return name;
    }

    public Player setHand(List<MahJong> hand) {
        this.hand = hand;
        return this;
    }

    public List<MahJong> getHand() {
        return hand;
    }

    public Set<MahJong> getTouchCards() {
        return touchCards;
    }

    public Set<MahJong> getMingGang() {
        return mingGang;
    }

    public Set<MahJong> getAnGang() {
        return anGang;
    }

    //出牌
    public MahJong play(int index){
        MahJong mahJong = hand.get(index);
        hand.remove(index);
        return mahJong;
    }

    //抓牌
    public void draw(MahJong mahJong){
        //排序插入
        SortUtil.insertAndSort(hand, mahJong);
    }

    //是否可以碰这张牌
    public boolean canPeng(MahJong mahJong){
        return MahJongUtil.findTotalOfMahJong(hand, mahJong) == 2;//如果手牌中有俩张这个牌则可以碰
    }

    //是否可以明杠这张牌
    public boolean canMingGang(MahJong mahJong){
        return MahJongUtil.findTotalOfMahJong(hand, mahJong) == 3;//如果手牌中有三张这个牌则可以碰
    }

    //是否可以巴杠这张牌,如果已经碰了这张牌,再抓到这张牌, 则可以巴杠
    public List<Integer> canBaGang(){
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++){
            if (touchCards.contains(hand.get(i))){
                indexs.add(i);
            }
        }
        return indexs.size() == 0 ? null : indexs;
    }

    //是否可以暗杠,手中有4张一样的牌则可以暗杠
    public List<Integer> canAnGang(){
        MahJong mahJong = hand.get(0);
        List<Integer> indexs = new ArrayList<>();
        int count = 1, length = hand.size();
        for (int i = 1; i < length + 1; i++){
            if (i < length && mahJong.equals(hand.get(i))){
                count++;
            }else {
                if (count == 4){
                    indexs.add(i - 4);//添加可暗杠的牌的起始下标
                }
                if (i < length){
                    count = 1;
                    mahJong = hand.get(i);
                }
            }
        }
        return indexs.size() == 0 ? null : indexs;
    }

    //碰牌
    public void touchCard(MahJong mahJong){
        int index, freq = 0;
        do {
            index = Collections.binarySearch(hand, mahJong);
            if (index < 0){
                break;
            }
            hand.remove(index);
            freq++;
        }while (freq < 2);
        if (freq == 2){//恰好删除俩张牌
            touchCards.add(mahJong);
        }else {
            LogUtil.error(Player.class, "log.error.play.action", name, mahJong, freq, mahJong, hand);
            throw new RuntimeException("log.error.play.action");
        }
    }
    //明杠牌
    public void mingGang(MahJong mahJong){
        int index, freq = 0;
        do {
            index = Collections.binarySearch(hand, mahJong);
            if (index < 0){
                break;
            }
            hand.remove(index);
            freq++;
        }while (freq < 3);
        if (freq == 3){//恰好删除三张牌
            mingGang.add(mahJong);
        }else {
            LogUtil.error(Player.class, "log.error.play.action", name, mahJong, freq, mahJong, hand);
            throw new RuntimeException("log.error.play.action");
        }
    }

    //巴杠
    public void baGang(int index){
        MahJong mahJong = hand.get(index);
        if (!touchCards.contains(mahJong)){
            throw new RuntimeException("the condition is not met, the action 'baGang' cannot be performed");
        }
        hand.remove(index);
        touchCards.remove(mahJong);
        mingGang.add(mahJong);
    }

    //暗杠
    public void anGang(int index){
        MahJong mahJong = hand.get(index);
        anGang.add(mahJong);
        for (int i = 0; i < 4; i++){//起点删除4次, 即从手牌中删除暗杠的牌
            hand.remove(index);
        }
    }

    public Set<Integer> getJiangCardSet() {
        return jiangCardSet;
    }

    public Player setJiangCardSet(Set<Integer> jiangCardSet) {
        assert jiangCardSet.size() == 2;
        this.jiangCardSet = jiangCardSet;
        return this;
    }

/**是否可以胡牌
 * 判断某个玩家当前的所有牌是否可胡. 胡牌一共14张牌， 3（刻子、顺子） * 4 + 2形式 (7个对子,可以不用连续)七对以及十三幺除外
 * 穷举所有将牌可能的情况, 看剩下的牌是否满足刻子或顺子
 * */
    public boolean canHu(){
        int keZiCount = touchCards.size() + mingGang.size() + anGang.size();
        int handSize = hand.size();
        assert handSize + keZiCount * 3 == 14;//如果false则表示发牌出问题
        if (HuPai.SSY.eligible(this)){//判断十三幺
            return true;
        }
        int bad = 4 - keZiCount;//剩下组成类型3的数量
        Map<Integer, Set<Integer>> allJiangSet = new HashMap<>();
        Set<Integer> special = null;
        MahJong mahJong = hand.get(0);
        int count = 1, freq = 0, dou = 0;
        for (int i = 1; i < handSize + 1; i++){//加一次循环,不然会漏掉最后一个元素的count
            if (i < handSize && hand.get(i).equals(mahJong)){
                count++;
            }else {
                if (count >= 2){//出现了2次以上
                    HashSet<Integer> set = new HashSet<>();//记录下作为将牌的俩个下标
                    set.add(i-1);
                    set.add(i-2);
                    allJiangSet.put(freq++, set);
                    dou++;//出现2张牌以上一样的情况, 对子数加1
                    if (count == 3){//满足一组刻子, 刻子剩余数-1
                        bad--;
                    }else if (count == 2){
                        special = set;
                    }else if (count == 4){
                        dou++;//出现一个4张牌一样的时候, 对子数再加1
                    }
                }
                if (i < handSize){
                    mahJong = hand.get(i);//更换比较的麻将
                    count = 1;//更换出现次数
                }
            }
        }
        if (bad == 0 && dou == 5 - keZiCount || dou == 7){//牌型为所有都是刻子加上一对将牌 4 - keZiCount + 1 或七对
            assert special != null;
            this.setJiangCardSet(special);
            return true;
        }
        List<MahJong> stack;
        for (int i = 0; i < freq; i++){
            stack = new ArrayList<>(12);
            Set<Integer> jiangSet = allJiangSet.get(i);
            //跳过作为将的牌, 判断剩下的牌是否能成为刻子、顺子
            for (int j = 0; j < handSize; j++){
                if (jiangSet.contains(j)){
                    continue;
                }
                stack.add(hand.get(j));
                if (stack.size() >= 3){
                    StackUtil.popKeZi(stack);
                    StackUtil.popShunZi(stack);
                }
            }
            if (stack.isEmpty()){//可以胡
                this.setJiangCardSet(jiangSet);//保存胡牌的将牌的位置下标
                return true;
            }
        }
        return false;
    }
}
