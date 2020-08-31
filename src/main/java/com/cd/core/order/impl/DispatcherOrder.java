package com.cd.core.order.impl;

import com.cd.core.cache.CoreLocalCache;
import com.cd.core.cache.impl.CoreLocalCacheImpl;
import com.cd.core.data.MessageData;
import com.cd.core.interceptor.ActionInterceptor;
import com.cd.core.interceptor.impl.ActionInterceptorImpl;
import com.cd.core.log.LogUtil;
import com.cd.core.order.Order;
import com.cd.core.pojo.Command;
import com.cd.core.utils.TipsUtil;
import com.cd.login.cmds.LoginCmd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class DispatcherOrder implements Order {
    private ActionInterceptor interceptor = new ActionInterceptorImpl();
    private CoreLocalCache localCache = CoreLocalCacheImpl.newInstance();
    private volatile static DispatcherOrder order;

    private DispatcherOrder() {}

    public static DispatcherOrder newInstance() {
        if(order == null) {
            synchronized (DispatcherOrder.class) {
                if(order == null) {
                    order = new DispatcherOrder();
                }
            }
        }
        return order;
    }

    @Override
    public MessageData execute(String handlerId, Command command, String[] params) {
        Object obj = command.getObj();
        Method method = command.getMethod();
        int length = method.getParameterCount();
        if(params.length < length - 1) {
            return new MessageData("core.order.fail.command1");
        }
        Object[] args;
        if(obj instanceof LoginCmd) {
            args = getNotLoggedArgs(handlerId, params, method);
        }else{
            boolean logged = interceptor.logged(handlerId);
            if(!logged) {
                return new MessageData("core.intercept.fail.login");
            }
            args = geAlreadyLoggedArgs(handlerId, params, method);
        }
        if (args == null){
            return new MessageData("core.order.fail.command2");
        }
        try {
            return (MessageData)method.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return new MessageData("system.error");
        }
    }

    private Object[] getNotLoggedArgs(String handlerId, String[] params, Method method) {
        int length = method.getParameterCount();
        Object[] args = new Object[length];
        args[0] = handlerId;
        Object[] args2 = getArgs(method, params);
        if(args2 == null) {
            return null;
        }
        System.arraycopy(args2, 0, args, 1, args2.length);
        return args;
    }

    private Object[] geAlreadyLoggedArgs(String handlerId, String[] params, Method method) {
        int length = method.getParameterCount();
        Object[] args = new Object[length];
        String val = localCache.getNameById(handlerId);
        if(val == null) {
            throw new RuntimeException(TipsUtil.getTip("log.error.core.order.msg1", handlerId)) ;
        }
        args[0] = val;
        Object[] args2 = getArgs(method, params);
        if(args2 == null) {
            return null;
        }
        System.arraycopy(args2, 0, args, 1, args2.length);
        return args;
    }

    private Object[] getArgs(Method method, String[] params) {
        Object[] args = new Object[method.getParameterCount() - 1];
        Parameter[] parameters = method.getParameters();
        int index = 0;
        for(int i = 1; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            String typeName = type.getTypeName();
            if(type.isArray()) {
                String[] arr = new String[params.length - index];
                System.arraycopy(params, index, arr, 0, arr.length);
                args[index] = arr;
                break;
            }else if(type.isPrimitive()){//如果是基本类型
                try {
                    switch(typeName) {
                        case "int" :
                            args[index] = Integer.parseInt(params[index]);break;
                        case "double" :
                            args[index] = Double.parseDouble(params[index]);break;
                        case "long" :
                            args[index] = Long.parseLong(params[index]);break;
                        case "boolean" :
                            args[index] = Boolean.parseBoolean(params[index]);break;
                        default :
                            return null;
                    }
                }catch(NumberFormatException e) {
                    LogUtil.error(this.getClass(), "log.error.core.order.msg2", method.getName(), parameters[i].getName());
                    return null;
                }
            }else if(typeName.startsWith("java.lang.")){
                if(typeName.endsWith(".String")) {
                    args[index] = params[index];
                }else{
                    try {
                        Class<?> forName = Class.forName(typeName);
                        Method m = forName.getMethod("valueOf", String.class);
                        if(m != null) {
                            Object obj = m.invoke(null, params[index]);
                            args[index] = obj;
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | SecurityException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                throw new RuntimeException(TipsUtil.getTip("log.error.core.order.msg3",  method.getName(), parameters[i].getName()));
            }
            index++;
        }
        return args;
    }

}
