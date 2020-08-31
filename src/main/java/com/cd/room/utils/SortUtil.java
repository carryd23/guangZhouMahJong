package com.cd.room.utils;

import java.util.List;

public class SortUtil {
    public static <T extends Comparable<T>> void insertSort(List<T> list){
        int j;
        for (int i = 1; i < list.size(); i++){
            T item = list.get(i);
            j = i - 1;
            while (j >= 0){
                if (list.get(j).compareTo(item) > 0){
                    j--;
                }else {
                    break;
                }
            }
            if (j != i-1){//表示位置应该移动
                list.remove(i);//从当前位置上移除
                list.add(j + 1, item);//将item添加在第一个不比它大的元素的后面
            }
        }
    }

    public static <T extends Comparable<T>> void insertAndSort(List<T> list, T item){
        int length = list.size() - 1;
        while (length >= 0){
            if (list.get(length).compareTo(item) > 0){
                length--;
            }else {
                break;
            }
        }
        list.add(length + 1, item);//将item添加在第一个不比它大的元素的后面
    }

    public static <T extends Comparable<T>> boolean isSorted(List<T> list){
        for (int i = list.size() -1; i > 0; i--){
            T right = list.get(i);
            T left = list.get(i - 1);
            if (right.compareTo(left) < 0){
                return false;
            }
        }
        return true;
    }
}
