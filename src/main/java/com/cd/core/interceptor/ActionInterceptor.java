package com.cd.core.interceptor;

public interface ActionInterceptor {
    //拦截方法,判断是否合法
    boolean logged(String handlerId);
}
