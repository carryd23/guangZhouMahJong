package com.cd.room.pojo;

import java.util.Objects;

public class MahJong implements Comparable<MahJong> {
    private int num;
    private MahJongType type;

    public MahJong(MahJongType type){
        this(-1, type);
    }

    public MahJong(int num, MahJongType type){
        this.num = num;
        this.type = type;
    }

    public int getNum() {
        return num;
    }

    public MahJongType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        MahJong mahJong = (MahJong) o;
        return num == mahJong.num &&
                type == mahJong.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, type);
    }

    @Override
    public int compareTo(MahJong o) {
        int compare = type.compareTo(o.type);
        return compare == 0 ? num - o.num : compare;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (num != -1){
            switch (num){
                case 1 :
                    sb.append("一");break;
                case 2 :
                    sb.append("二");break;
                case 3 :
                    sb.append("三");break;
                case 4 :
                    sb.append("四");break;
                case 5 :
                    sb.append("五");break;
                case 6 :
                    sb.append("六");break;
                case 7 :
                    sb.append("七");break;
                case 8 :
                    sb.append("八");break;
                case 9 :
                    sb.append("九");break;
            }
        }
        sb.append(type.toString());
        return sb.toString();
    }
}
