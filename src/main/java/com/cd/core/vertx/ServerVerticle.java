package com.cd.core.vertx;

import com.cd.core.data.MessageData;
import com.cd.core.log.LogUtil;
import com.cd.core.order.Order;
import com.cd.core.order.impl.DispatcherOrder;
import com.cd.core.pojo.Command;
import com.cd.core.utils.CommandHashMap;
import com.cd.core.utils.MessageUtil;
import com.cd.core.utils.TipsUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

import java.util.ArrayList;
import java.util.List;

import static com.cd.core.utils.ConstantUtil.TCP_HOST;
import static com.cd.core.utils.ConstantUtil.TCP_PORT;
import static com.cd.core.utils.ConstantUtil.CHARSET;
import static com.cd.core.utils.ConstantUtil.LINE_SEPARATOR;
import static com.cd.core.utils.ConstantUtil.SIGN_OUT;

public class ServerVerticle extends AbstractVerticle {
    private CommandHashMap map = CommandHashMap.newInstance();
    private Order order = DispatcherOrder.newInstance();
    private MessageUtil msgHelper = MessageUtil.newInstance();
    @Override
    public void start() {
        NetServerOptions options = new NetServerOptions();
        options
                .setPort(TCP_PORT)
                .setHost(TCP_HOST)
                .setLogActivity(true);//开启网络记录
        NetServer netServer = vertx.createNetServer(options);

        netServer.connectHandler(socket -> {
            LogUtil.info(ServerVerticle.class,"log.info.server.msg1", socket.remoteAddress());
            StringBuilder commandChar = new StringBuilder();
            //用户写入数据时回调
            socket.handler(buffer -> {
                byte[] bytes = buffer.getBytes();
                if(bytes.length == 2 && bytes[0] == 13 && bytes[1] == 10) {//换行符
                    String command = commandChar.toString().trim();
                    if(command.length() == 0) {
                        return;//空命令
                    }
                    String[] commands = command.split(" +");
                    vertx.executeBlocking(handler -> {
                        Command cmd = map.get(commands[0]);
                        if(cmd == null) {
                            handler.complete(new MessageData("core.server.fail.msg1"));
                        }else {
                            MessageData result = order.execute(socket.writeHandlerID(), cmd, getParams(commands));
                            handler.complete(result);
                        }
                    }, false, res -> {
                        if(res.failed()) {
                            LogUtil.error(ServerVerticle.class, "log.error.server.msg1", res.cause());
                        }
                        MessageData result = (MessageData)res.result();
                        if(result == null) {
                            return;
                        }
                        String name = result.getName();
                        String tip = result.getMsg();
                        String msg = null;
                        if(tip != null) {
                            msg = TipsUtil.getTip(tip, result.getArgs());
                        }
                        if(name == null && msg != null) {//发送给自己的信息
                            socket.write(msg + LINE_SEPARATOR, CHARSET);
                        }else if(name != null && msg != null) {//发送给他人的信息
                            msgHelper.sendMsg(name, msg);
                        }
                        if(result.getOthers() == null) {
                            return;
                        }
                        MessageData[] others = result.getOthers();
                        for(MessageData other : others) {
                            msgHelper.sendMsg(other.getName(),  TipsUtil.getTip(other.getMsg(), other.getArgs()));
                        }
                    });

                    commandChar.delete(0, commandChar.length());//删除之前的命令
                }else if(bytes[0] == 8 && bytes.length == 1) {
                    //backspace
                    try {
                        commandChar.deleteCharAt(commandChar.length() - 1);
                    }catch(StringIndexOutOfBoundsException ignored) {
                    }
                }else {
                    commandChar.append(buffer.toString(CHARSET));//加入命令
                }
            });

            socket.write(TipsUtil.getTip("core.server.success.init") + LINE_SEPARATOR, CHARSET);

            socket.closeHandler(close -> {
                vertx.executeBlocking(handler -> {
                    Command cmd = map.get(SIGN_OUT);
                    MessageData result = order.execute(socket.writeHandlerID(), cmd, new String[0]);
                    handler.complete(result);
                }, false, res -> {
                    if(res.failed()) {
                        LogUtil.error(ServerVerticle.class, TipsUtil.getTip("log.error.server.msg3"));
                    }
                    MessageData result = (MessageData)res.result();
                    if(result == null || result.getOthers() == null) {
                        return;
                    }
                    MessageData[] others = result.getOthers();
                    for(MessageData other : others) {
                        msgHelper.sendMsg(other.getName(), TipsUtil.getTip(other.getMsg(), other.getArgs()));
                    }
                });
            });
        });

        netServer.listen(server -> {
            if (server.succeeded()) {
                LogUtil.info(ServerVerticle.class, "log.info.server.msg2", server.result().actualPort());
            }else {
                LogUtil.error(ServerVerticle.class, "log.error.server.msg2", server.cause());
            }
        });
    }

    //去掉代表命令的commands[0]，获取请求参数
    private String[] getParams(String[] commands) {
        List<String> list = new ArrayList<>();
        for(int i = 1; i < commands.length; i++) {
            list.add(commands[i]);
        }
        String[] params = new String[list.size()];
        return list.toArray(params);
    }
}
