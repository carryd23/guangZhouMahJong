package com.cd.room.pojo;

import com.cd.room.cache.MahJongLocalCache;
import com.cd.room.cache.impl.MahJongLocalCacheImpl;
import com.cd.room.utils.CircularQueue;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Room {
    private static MahJongLocalCache mahJongLocalCache = MahJongLocalCacheImpl.newInstance();
    private int id;//房间号
    private CircularQueue<Player> cycle;//保存玩家的循环数组
    private AtomicReference<String> owner = new AtomicReference<>("");//房主
    private ConcurrentHashMap<String, Integer> seatChart = new ConcurrentHashMap<>();//座位表, name到数组下标的映射
    private AtomicReference<MahJong> lastCard = new AtomicReference<>(new MahJong(null));//该房间上一次出的牌
    private int lastPlayer;//该房间上一个出牌人
    private AtomicReference<ActionTask> actionTask = new AtomicReference<>(new ActionTask());
    private AtomicBoolean started;//游戏是否已经开始
    private int banker;//庄家对应的位置下标, 刚开始设置为房主
    private List<List<MahJong>> mahJongList;//房间的麻将
    private int[] index = new int[2];//房间的麻将的下标
    private int[] dice = new int[2];//记录本房间所掷骰子

    public Room(String owner){
        this.owner.set(owner);//设置房主
        cycle = new CircularQueue<>();//创建一个4人循环数组
        int i = cycle.enQueue(new Player(owner));//房主占一个位置
        banker = i;//初始房主为庄家
        lastPlayer = banker;//初始出牌人应该是庄家,即房主
        seatChart.put(owner, i);//保存房主的座位表
        id = ThreadLocalRandom.current().nextInt(1000, 10000);//创建房间唯一id
        started = new AtomicBoolean(false);
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner.get();
    }

    public Player getBanker() {
        return cycle.get(banker);
    }

    /**将某个玩家设置为庄家,实际设置的是该玩家对应的位置下标。
     * 这样,即使庄家玩家退出，该位置的另一个依然是庄家
     * */
    public Room setBanker(String name) {
        Integer integer = seatChart.get(name);
        banker = integer;
        return this;
    }

    public MahJong getLastCard() {
        return lastCard.get();
    }

    public Room setLastCard(MahJong mahJong) {
        lastCard.set(mahJong);
        return this;
    }

    /**轮到谁出牌, 应该是上一个出牌人的下家
     * */
    public Player getWhoseTurn(){
        return cycle.next(lastPlayer);
    }

    /**现在是谁在出牌
     * */
    public void setWhoseTurn(String name){
        lastPlayer = seatChart.get(name);
    }

    public Room setActionTask(String name, ActionTask.TaskType type) {
        cleanAllActionTask();
        actionTask.get().setPerformer(name).setTaskType(type);
        return this;
    }

    public Room addActionTask(ActionTask.TaskType type){
        actionTask.get().setTaskType(type);
        return this;
    }

    public void cleanAllActionTask(){
        actionTask.get().removeAllTask();
    }


    public String getActionPerformer(){
        return actionTask.get().getPerformer();
    }

    public List<ActionTask.TaskType> getActionTaskType() {
        return actionTask.get().getTaskType();
    }

    public Room setStarted(boolean started) {
        this.started.set(started);
        return this;
    }

    public boolean getStarted() {
        return started.get();
    }


    public boolean add(String name){
        int i = cycle.enQueue(new Player(name));
        if (i == -1){
            return false;
        }
        Integer absent = seatChart.putIfAbsent(name, i);
        return absent == null;
    }

    public boolean addRobot(){
        String robotName = "Robot-"+ ThreadLocalRandom.current().nextInt(1, 100);
        int i = cycle.enQueue(new Player(robotName).setIsRobot(true));
        if (i == -1){
            return false;
        }
        Integer absent = seatChart.putIfAbsent(robotName, i);
        return absent == null;
    }

    public void remove(String name){
        Integer index = seatChart.remove(name);
        cycle.deQueue(index);
    }

    public List<Player> getAllNonRobotPlayer(){
        Enumeration<String> keys = seatChart.keys();
        List<Player> others = new ArrayList<>(4);
        while (keys.hasMoreElements()){
            String element = keys.nextElement();
            Player player = getPlayerByName(element);
            if (player == null || player.isRobot()){//获取除指定名字外的其他非机器人用户
                continue;
            }
            others.add(player);
        }
        return others;
    }

    public int getRobotCount(){
        int count = 0;
        for (int i = 0; i < cycle.length(); i++){
            Player player = cycle.get(i);
            if (player != null && player.isRobot()){
                count++;
            }
        }
        return count;
    }

    //获取剩余空位
    public int getRemainingSeat(){
        int count = 0;
        for (int i = 0; i < cycle.length(); i++){
            Player player = cycle.get(i);
            if (player == null || player.isRobot()){
                count++;
            }
        }
        return count;
    }

    /**更换房主
     * */
    public boolean changeOwner(){
        Integer index = seatChart.get(owner.get());
        Player next = cycle.nextNoRobot(index);
        if (next == null){
            return false;
        }
        this.owner.set(next.getName());
        return true;
    }

    public void roomInit(){
        //获取麻将
        mahJongList = mahJongLocalCache.getMahJong();
        //掷骰子
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int i = random.nextInt(1, 7);
        int i1 = random.nextInt(1, 7);
        int max, min;
        max = Math.max(i, i1);
        min = i == max ? i1 : i;
        index[0] = (max - 1) % mahJongList.size();//牌堆
        index[1] = min * 2;//抓牌的起点
        dice[0] = index[0];
        dice[1] = index[1] - 1;//最后一张牌所在下标
    }

/*    //掷骰子
    public void rollTheDice(){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int i = random.nextInt(1, 7);
        int i1 = random.nextInt(1, 7);
        int max, min;
        max = i > i1 ? i : i1;
        min = i == max ? i1 : i;
        dice = index = new int[2];
        index[0] = (max - 1) % mahJongList.size();//牌堆
        index[1] = min * 2;//抓牌的起点
        dice[0] = index[0];
        dice[1] = index[1] - 1;//最后一张牌所在下标
    }*/

    /**获取指定玩家的下一个玩家
     * */
    public Player nextPlayer(String name){
        Integer integer = seatChart.get(name);
        return cycle.next(integer);
    }

    public Player nextPlayer(Player player){
        return nextPlayer(player.getName());
    }

    /**通过名字获取对应的Player
     * */
    public Player getPlayerByName(String name){
        Integer integer = seatChart.get(name);
        return cycle.get(integer);
    }

    public int getTotalOfPlayers(){
        return cycle.length();
    }

    //正序发出指定数量张牌
    public List<MahJong> licensing(int num){
        if (num <= 0){
            throw new IllegalArgumentException("licensing arg 'num' must gt 0");
        }
        int j = index[1];
        List<MahJong> returnList;
        List<MahJong> list = mahJongList.get(index[0]);
        if (j + num <= list.size()){//直接从这个list中拿
            returnList = list.subList(j, j + num);
            index[1] = j + num;
            if (index[1] == list.size()){
                index[0] = (index[0] + 1) % mahJongList.size();//更新指向的list
                index[1] = 0;//更新指向的下标未下一个list的第一个元素
            }
        }else {
            returnList = list.subList(j, list.size());//从list中那一部分
            int bad = num - returnList.size();//计算差值
            index[0] = (index[0] + 1) % mahJongList.size();//更新指向的牌堆的下标
            List<MahJong> subList = mahJongList.get(index[0]).subList(0, bad);//从下一个list中拿一部分
            returnList.addAll(subList);
            index[1] = bad;//更新指向的牌的下标
        }
        return returnList;
    }

    //头发出一张牌
    public MahJong licensingHead(){
        return licensing(1).get(0);
    }

    //末尾发出一张牌
    public MahJong licensingEnd(){
        List<MahJong> list = mahJongList.get(dice[0]);
        MahJong mahJong =  list.get(dice[1]);
        dice[1] = dice[1] - 1;
        if(dice[1] < 0){
            dice[0] = (dice[0] + mahJongList.size() - 1) % mahJongList.size();//更新指向的list
            dice[1] = 33;//指向下个list的最后一个位置
        }
        return mahJong;
    }

    //当俩个指针数组重复则表示没有剩余的牌了
    public boolean mahJongRunOut(){
        return dice[0] == index[0] && dice[1] + 1 == index[1];//不加1,则最后一张牌不会被抓
    }

    //获取剩余牌的数量
    public int getRemainingMahJong(){
        int i = dice[0], freq = 0;
        do {
            i = (i + 1) % mahJongList.size();
            freq++;
        }while (i != index[0]);//相差几个34
        return 137 - 34 * freq - index[1] + dice[1];
//        if (dice[0] == index[0]){//表示在同一个list中
//            if (dice[1] - index[1] >= 0){//表示骰子数指向的起点在牌指针后面,说明抓完了一圈又抓到了起点所指的list
//                return dice[1] - index[1] + 1;
//            }else {//表示才刚开始, 并没有抓完一圈
//                return 135 + dice[1] - index[1];//3*34表示其他三个list中的牌 dice[1]+1 表示0-起点, 34-(index[1]-dice[1]-1)表示该list已经被抓走的牌
//            }
//        }else {//并没有指向同一个list
//            int i = index[0] - dice[0] - 1;//相差几个list, 减去index[0]所指向的那个list
//            //34 - dice[1] - 1 表示起点所指向的那个list中被抓走的牌的数量
//            //34 - index[1] 表示index[0]指向的那个list中,被抓走的数量
//            //136总数 - 用了的牌
//            return 103 - 34 * i + dice[1] - index[1];
//        }
    }
}
