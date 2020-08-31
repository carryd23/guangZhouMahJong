package com.cd.core.utils;

import com.cd.core.anno.Cmd;
import com.cd.core.pojo.Command;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class CmdClassLoader {

    private static CmdClassScanner scanner = new CmdClassScanner();

    public static void loadClass(){
        CommandHashMap map = CommandHashMap.newInstance();
        //获得当前项目下除core包外所有的类的全类名
        List<String> classNames = scanner.getClassNames();
        for(String name : classNames) {
            try {
                Class<?> cl = Class.forName(name);
                Cmd cmdType = cl.getAnnotation(Cmd.class);
                if(cmdType == null || cl.isInterface() || Modifier.isAbstract(cl.getModifiers())) {
                    continue;
                }
                Object obj = cl.newInstance();
                Method[] methods = cl.getMethods();
                for(Method method : methods) {
                    Cmd fun = method.getDeclaredAnnotation(Cmd.class);
                    if(fun == null) {
                        continue;
                    }
                    map.put(fun.value(), new Command(obj, method));
                }
            }catch(ClassNotFoundException e) {
                throw new RuntimeException(e);
            }catch(InstantiationException e) {
                throw new RuntimeException("newInstance throw Exception");
            }catch(IllegalAccessException e) {
                throw new RuntimeException("newInstance throw Exception");
            }
        }
    }
}
