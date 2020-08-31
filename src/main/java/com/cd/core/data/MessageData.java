package com.cd.core.data;

public class MessageData {
    private String name; //需要发送给某个人的消息，代表某个人, 为null代表发给自己
    private String msg;//发送的消息
    private Object[] args = new Object[0]; //发送消息附带的参数
    private MessageData[] others;//发送其他的消息，其中的name代表另一个人的name, msg代表发给另一个人的消息

    public MessageData() {}

    public MessageData(String msg) {
        this.msg = msg;
    }

    public MessageData(String name, String msg) {
        this.name = name;
        this.msg = msg;
    }

    public MessageData(String name, String msg, Object...args) {
        this.name = name;
        this.msg = msg;
        this.args = args;
    }


    public String getName() {
        return name;
    }

    public MessageData setName(String name) {
        this.name = name;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public MessageData setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public MessageData[] getOthers() {
        return others;
    }

    public MessageData setOthers(MessageData[] others) {
        this.others = others;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    public MessageData setArgs(Object...args) {
        this.args = args;
        return this;
    }
}
