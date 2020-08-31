package com.cd.room.pojo;

import com.cd.room.utils.MahJongUtil;
import com.cd.room.utils.SortUtil;
import com.cd.room.utils.StackUtil;

import java.util.*;

public enum HuPai {
    JI{//满足麻将规则的牌都可以胡
        @Override
        public String toString() {
            return "鸡胡";
        }

        @Override
        public int getScore() {
            return 1;
        }

        @Override
        public boolean eligible(Player player) {
            return true;//只要判断能胡牌, 则绝对满足鸡胡
        }
    },
    PING{//全部都是顺子而没有刻子,即不能有碰、杠的牌
        @Override
        public String toString() {
            return "平胡";
        }

        @Override
        public int getScore() {
            return 2;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            if (hand.size() != 14){
                return false;
            }
            Set<Integer> jiangCardSet = player.getJiangCardSet();
            List<MahJong> others = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++){
                if (jiangCardSet.contains(i)){
                    continue;
                }
                others.add(hand.get(i));
                if (others.size() >= 3){
                    StackUtil.popShunZi(others);
                }
            }
            return others.isEmpty();
        }
    },
    PP{//全部是刻子，没有顺子
        @Override
        public String toString() {
            return "碰碰胡";
        }

        @Override
        public int getScore() {
            return 8;
        }

        @Override
        public boolean eligible(Player player) {
            Set<Integer> jiangCardSet = player.getJiangCardSet();
            List<MahJong> hand = player.getHand();
            List<MahJong> others = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++){
                if (jiangCardSet.contains(i)){
                    continue;
                }
                others.add(hand.get(i));
                if (others.size() >= 3){
                    StackUtil.popKeZi(others);
                }
            }
            return others.isEmpty();
        }
    },
    HYS{//由字牌及另外单一花色(筒、索、万)组成
        @Override
        public String toString() {
            return "混一色";
        }

        @Override
        public int getScore() {
            return 8;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> other = new ArrayList<>();
            filter(player.getHand(), other);
            filter(player.getTouchCards(), other);
            filter(player.getMingGang(), other);
            filter(player.getAnGang(), other);
            if (other.size() <= 0){
                return false;
            }
            MahJongType color = other.get(0).getType();//如果other.size == 0 则该副牌应该满足字一色,则不会进入该方法所以不用判断长度
            for (int i = 1; i < other.size(); i++){
                if (other.get(i).getType() != color){//单一花色
                    return false;
                }
            }
            return true;
        }

        private void filter(Collection<MahJong> before, List<MahJong> after){
            for (MahJong mahJong : before){
                if (mahJong.getNum() != -1){//选出所有不是字的牌
                    after.add(mahJong);
                }
            }
        }
    },
    HP{//混一色+碰碰胡
        @Override
        public String toString() {
            return "混碰";
        }

        @Override
        public int getScore() {
            return 32;
        }

        @Override
        public boolean eligible(Player player) {
            return HYS.eligible(player) && PP.eligible(player);
        }
    },
    QYS{//整副牌统一花色
        @Override
        public String toString() {
            return "清一色";
        }

        @Override
        public int getScore() {
            return 32;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            MahJong mahJong = hand.get(0);
            for (int i = 1; i < hand.size(); i++){
                MahJong jong = hand.get(i);
                if (jong.getType() != mahJong.getType()){
                    return false;
                }
            }
            for (MahJong jong : player.getTouchCards()){
                if (jong.getType() != mahJong.getType()){
                    return false;
                }
            }

            for (MahJong jong : player.getMingGang()){
                if (jong.getType() != mahJong.getType()){
                    return false;
                }
            }

            for (MahJong jong : player.getAnGang()){
                if (jong.getType() != mahJong.getType()){
                    return false;
                }
            }

            return true;
        }

    },
    QD{//七个对子,该种牌型不能有碰牌、杠牌

        @Override
        public String toString() {
            return "七对";
        }

        @Override
        public int getScore() {
            return 32;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            int handSize = hand.size();
            if (handSize != 14){
                return false;
            }
            assert SortUtil.isSorted(hand);
            MahJong mahJong = hand.get(0);
            int count = 1, sum = 0;//记录出现的对子数
            for (int i = 1; i < handSize + 1; i++){//加一次循环, 不然会漏掉最后一个元素
                if (i < handSize && hand.get(i).equals(mahJong)){
                    count++;
                }else {
                    if (count == 2){
                        sum++;
                    }
                    if (i < handSize){
                        mahJong = hand.get(i);
                        count = 1;
                    }
                }
            }
            return sum == 7;
        }
    },
    DQD{//手牌中至少有一个 4个牌, 其余的都是对子
        @Override
        public String toString() {
            return "豪华大七对";
        }

        @Override
        public int getScore() {
            return 64;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            int handSize = hand.size();
            if (handSize != 14){
                return false;
            }
            int count = 1, four = 0, dou = 0;
            MahJong mahJong = hand.get(0);
            for (int i = 1; i < handSize + 1; i++){//加一次循环，不然会漏掉最后一个元素
                if (i < handSize && hand.get(i).equals(mahJong)){
                    count++;
                }else {
                    if (count == 2){
                        dou++;
                    }else if (count == 4){
                        four++;
                    }
                    if (i < handSize){
                        mahJong = hand.get(i);
                        count = 1;
                    }
                }
            }
            return four > 0 && four * 4 + dou * 2 == 14;
        }
    },
    QP{//清一色+碰碰胡
        @Override
        public String toString() {
            return "清碰";
        }

        @Override
        public int getScore() {
            return 64;
        }

        @Override
        public boolean eligible(Player player) {
            return QYS.eligible(player) && PP.eligible(player);
        }
    },
    HYJ{//由1 9牌以及字牌组成的类型
        @Override
        public String toString() {
            return "混幺九";
        }

        @Override
        public int getScore() {
            return 64;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            if (noHYJ(hand)){
                return false;
            }
            if (noHYJ(player.getTouchCards())){
                return false;
            }
            if (noHYJ(player.getMingGang())){
                return false;
            }
            return !noHYJ(player.getAnGang());
        }

        private boolean noHYJ(Collection<MahJong> mahJongs){
            for (MahJong mahJong : mahJongs){
                int num = mahJong.getNum();
                if (num != 1 && num != 9 && num != -1){
                    return true;
                }
            }
            return false;
        }
    },
    XSY{//中、发、白其中一种是将，其他的都是刻子
        @Override
        public String toString() {
            return "小三元";
        }

        @Override
        public int getScore() {
            return 128;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> yuan = new ArrayList<>();
            yuan.add(new MahJong(MahJongType.ZHONG));
            yuan.add(new MahJong(MahJongType.FA));
            yuan.add(new MahJong(MahJongType.BAI));
            return MahJongUtil.delJiangAndFindTotalKeZi(player, yuan);
        }
    },
    XSX{//4种风牌种有3种是刻子、1种是将牌
        @Override
        public String toString() {
            return "小四喜";
        }

        @Override
        public int getScore() {
            return 128;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> feng = new ArrayList<>();
            feng.add(new MahJong(MahJongType.FENG_D));
            feng.add(new MahJong(MahJongType.FENG_N));
            feng.add(new MahJong(MahJongType.FENG_X));
            feng.add(new MahJong(MahJongType.FENG_B));
            return MahJongUtil.delJiangAndFindTotalKeZi(player, feng);
        }
    },
    ZYS{//7种字牌合成的清一色
        @Override
        public String toString() {
            return "字一色";
        }

        @Override
        public int getScore() {
            return 256;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            if (noWordCard(hand)){
                return false;
            }
            Set<MahJong> touchCards = player.getTouchCards();
            if (noWordCard(touchCards)){
                return false;
            }
            Set<MahJong> mingGang = player.getMingGang();
            if (noWordCard(mingGang)){
                return false;
            }
            Set<MahJong> anGang = player.getAnGang();
            return !noWordCard(anGang);
        }

        private boolean noWordCard(Collection<MahJong> mahJongs){
            for (MahJong mahJong : mahJongs){
                if (mahJong.getNum() != -1){
                    return true;
                }
            }
            return false;
        }
    },
    QYJ{//只有1 9筒索万
        @Override
        public String toString() {
            return "清幺九";
        }

        @Override
        public int getScore() {
            return 256;
        }

        @Override
        public boolean eligible(Player player) {
            List<MahJong> hand = player.getHand();
            if (noOneAndNine(hand)){
                return false;
            }
            Set<MahJong> touchCards = player.getTouchCards();
            if (noOneAndNine(touchCards)){
                return false;
            }
            Set<MahJong> mingGang = player.getMingGang();
            if (noOneAndNine(mingGang)){
                return false;
            }
            Set<MahJong> anGang = player.getAnGang();
            return !noOneAndNine(anGang);
        }

        private boolean noOneAndNine(Collection<MahJong> mahJongs){
            for (MahJong mahJong : mahJongs){
                int num = mahJong.getNum();
                if (num != 1 && num != 9){
                    return true;
                }
            }
            return false;
        }
    },
    DSY{//中、发、白3组刻子
        private final List<MahJong> dsy = new ArrayList<>();
        {
            dsy.add(new MahJong(MahJongType.ZHONG));
            dsy.add(new MahJong(MahJongType.FA));
            dsy.add(new MahJong(MahJongType.BAI));
        }
        @Override
        public String toString() {
            return "大三元";
        }

        @Override
        public int getScore() {
            return 512;
        }

        @Override
        public boolean eligible(Player player) {
            int totalOfKeZi = MahJongUtil.findTotalOfKeZi(player, dsy);
            return totalOfKeZi == dsy.size();
        }
    },
    DSX{//4组风牌刻子
        private final List<MahJong> feng = new ArrayList<>();
        {
            feng.add(new MahJong(MahJongType.FENG_D));
            feng.add(new MahJong(MahJongType.FENG_N));
            feng.add(new MahJong(MahJongType.FENG_X));
            feng.add(new MahJong(MahJongType.FENG_B));
        }
        @Override
        public String toString() {
            return "大四喜";
        }

        @Override
        public int getScore() {
            return 512;
        }

        @Override
        public boolean eligible(Player player) {
            int total = MahJongUtil.findTotalOfKeZi(player, feng);
            return total == feng.size();
        }
    },
    SSY{//十三幺 1 9筒索万 风牌 、字牌 以上任意一张作为将
        private final Set<MahJong> ssy = new HashSet<>(13);
        {
            ssy.add(new MahJong(1, MahJongType.TONG));
            ssy.add(new MahJong(9, MahJongType.TONG));
            ssy.add(new MahJong(1, MahJongType.SUO));
            ssy.add(new MahJong(9, MahJongType.SUO));
            ssy.add(new MahJong(1, MahJongType.WANG));
            ssy.add(new MahJong(9, MahJongType.WANG));
            ssy.add(new MahJong(MahJongType.FENG_D));
            ssy.add(new MahJong(MahJongType.FENG_N));
            ssy.add(new MahJong(MahJongType.FENG_X));
            ssy.add(new MahJong(MahJongType.FENG_B));
            ssy.add(new MahJong(MahJongType.ZHONG));
            ssy.add(new MahJong(MahJongType.FA));
            ssy.add(new MahJong(MahJongType.BAI));
        }
        @Override
        public String toString() {
            return "十三幺";
        }

        @Override
        public int getScore() {
            return 512;
        }

        @Override
        public boolean eligible(Player player){
            List<MahJong> hand = player.getHand();
            if (hand.size() != 14){//十三幺不能有碰、杠等牌
                return false;
            }
            //只能出现13种类型的牌,最多只能有俩个一样的牌即将牌
            int count = 0, length = hand.size();
            for (int i = 1; i < length; i++){
                if (!hand.get(i).equals(hand.get(i - 1))){
                    count++;
                }
            }
            if (!hand.get(0).equals(hand.get(length - 1))){
                count++;
            }
            if (count != 13){
                return false;
            }
            for (MahJong mahJong : hand) {//如果这13中类型的牌中有不在十三幺范围内的, 返回false
                if (!ssy.contains(mahJong)) {
                    return false;
                }
            }
            return true;
        }
    };
    public abstract int getScore();//胡牌分值
    public abstract boolean eligible(Player player);
}
