package com.cd.room.utils;

import com.cd.room.pojo.MahJong;

import java.util.List;

public class StackUtil {
    public static void popKeZi(List<MahJong> arr){
        if (arr.size() < 3){
            return;
        }
        //判断是否满足刻子
        int size = arr.size(), count = 0;
        MahJong mahJong = arr.get(size - 1);
        for (int i = size - 2; i >= size - 3; i--){//倒数第一个分别与倒数2、3个进行比较
            if (arr.get(i).equals(mahJong)){
                count++;
            }
        }
        if (count == 2){//满足刻子
            for (int i = 0; i < 3; i++){
                arr.remove(arr.size() - 1);//删除倒数三个
            }
        }
    }

    public static void popShunZi(List<MahJong> arr){
        if (arr.size() < 3){
            return;
        }
        int size = arr.size();
        MahJong mahJong = arr.get(size - 1);
        //判断是否满足顺子
        int first = -1, mid = -1;//保存作为顺子的前俩张牌的下标, 第三张牌的下标应该为size - 1即刚加进来的那一张牌
        for (int i = size -2; i >= 0; i--){
            MahJong left = arr.get(i);
            if (left.getType() == mahJong.getType()){//类型相同
                int leftNum = left.getNum();
                int num = mahJong.getNum();
                if (mid == -1 && leftNum == num - 1){
                    mid = i;
                }else if (first == -1 && leftNum == num - 2){
                    first = i;
                }
            }
        }
        if (first != -1 && mid != -1){
            arr.remove(size -1);
            arr.remove(mid);
            arr.remove(first);
        }
    }
}
