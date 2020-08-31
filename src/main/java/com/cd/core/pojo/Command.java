package com.cd.core.pojo;

import java.lang.reflect.Method;

public class Command {
    private Object obj;
    private Method method;

    public Command(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
    }

    public Object getObj() {
        return obj;
    }

    public Command setObj(Object obj) {
        this.obj = obj;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public Command setMethod(Method method) {
        this.method = method;
        return this;
    }
}
