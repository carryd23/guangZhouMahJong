package com.cd.room.utils;

import com.cd.room.pojo.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 循环队列
 */
public class CircularQueue<T extends Player> {
    private AtomicReferenceArray<T> arr;
    private int length;
    private AtomicInteger index;//初始下标指向0

    public CircularQueue(){
        this(4);
    }

    public CircularQueue(int length){
        arr = new AtomicReferenceArray<>(length);
        this.length = length;
        index = new AtomicInteger(0);
    }

    public int enQueue(T item){
        int oldVal,newVal = -1,count=0;
        boolean isNotNull;
        do{
            isNotNull = false;
            if (count == length){//连续遍历了length个位置都没有空位
                return -1;
            }
            oldVal = index.get();
            if (arr.get(oldVal) == null || arr.get(oldVal).isRobot()){//如果当前位置为空或者是一个机器人, 则试着进入该位置
                newVal = (oldVal + 1) % length;
                continue;
            }
            count++;//否则次数加1
            isNotNull = true;
        }while (isNotNull || !index.compareAndSet(oldVal, newVal));//试着抢占该位置
        arr.set(oldVal, item);//抢占成功
        return oldVal;
    }

    public void deQueue(int i){
        arr.set(i, null);
    }

    //返回指定下标的下一个T 在游戏开始是不会返回null
    public T next(int i){
        return arr.get((i + 1) % length);
    }

    public T get(int index){
        return arr.get(index);
    }

    public T nextNoRobot(int i){
        int freq = 0;
        T item;
        do {
            i = (i + 1) % length;
            item = arr.get(i);
            freq++;
        }while (freq < length - 1 && (item == null || item.isRobot()));
        return item;
    }

    public int length(){
        return length;
    }
}
