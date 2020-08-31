package com.cd.login.utils;

import com.cd.core.model.CoreModel;

public class LoginUtil {
    private static CoreModel coreModel = CoreModel.newInstance();

    public static boolean actionRepeat(String handlerId){
        return coreModel.containsId(handlerId);
    }

    public static boolean alreadyLogged(String name){
        return coreModel.containsName(name);
    }

    public static boolean goOnline(String handlerId, String name){
        return coreModel.goOnline(handlerId, name);
    }

    public static String offLine(String handlerId){
        return coreModel.offLine(handlerId);
    }
}
