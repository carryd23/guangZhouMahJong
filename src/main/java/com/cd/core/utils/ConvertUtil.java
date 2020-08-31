package com.cd.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

public class ConvertUtil {
    public static void convertField(Object obj, Field field, String val){
        try {
            field.setAccessible(true);
            Class<?> type = field.getType();
            String typeName = type.getTypeName();
            if(type.isPrimitive()) {
                switch(typeName) {
                    case "int" :
                        field.setInt(obj, Integer.parseInt(val)); break;
                    case "long" :
                        field.setLong(obj, Long.parseLong(val)); break;
                    case "double" :
                        field.setDouble(obj, Double.parseDouble(val)); break;
                    case "boolean" :
                        field.setBoolean(obj, Boolean.parseBoolean(val)); break;
                }
            }else if(typeName.startsWith("java.lang.")){
                if(typeName.endsWith(".String")) {
                    field.set(obj, val);
                }else{
                    Class<?> forName = Class.forName(typeName);
                    Method m = forName.getMethod("valueOf", String.class);
                    if(m != null) {
                        Object returnVal = m.invoke(null, val);
                        field.set(obj, returnVal);
                    }
                }
            }else {
                throw new RuntimeException(MessageFormat.format("The complex class {0} cannot be converted and needs to be extended", obj.getClass().getName())) ;
            }
        }catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e){
            throw new RuntimeException(e);
        }
    }
}
