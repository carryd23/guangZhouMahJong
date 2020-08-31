package com.cd.core.dao;

import com.cd.core.utils.ConvertUtil;
import com.cd.core.utils.JDBCUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.text.MessageFormat;

public class DaoBase {

    public <T> T querySql(Class<T> cla, String sql, Object...args) {
        ResultSet resultSet = null;
        try(Connection conn = JDBCUtil.getConnection();
            PreparedStatement stat = conn.prepareStatement(sql)){
            for(int i = 0; i < args.length; i++) {
                stat.setObject(i+1, args[i]);
            }
            resultSet = stat.executeQuery();
            if(!resultSet.next()) {
                return null;
            }
            ResultSetMetaData metaData = resultSet.getMetaData();
            try {
                T obj = cla.newInstance();
                Field[] fields = cla.getDeclaredFields();
                for(int i = 0; i < fields.length; i++) {
                    String label = metaData.getColumnLabel(i + 1);
                    Field field = cla.getDeclaredField(label);
                    String val = resultSet.getString(label);
                    ConvertUtil.convertField(obj, field, val);
                }
                return obj;
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }finally {
            JDBCUtil.close(resultSet);
        }
    }

    public int modifySql(String sql, Object...args){
        try(Connection conn = JDBCUtil.getConnection();
            PreparedStatement stat = conn.prepareStatement(sql)){
            for (int i = 0; i < args.length; i++){
                stat.setObject(i + 1, args[i]);
            }
            return stat.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }
}
