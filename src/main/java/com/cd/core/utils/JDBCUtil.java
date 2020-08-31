package com.cd.core.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtil {

    private volatile static DataSource ds;

    public static void initDataSource() {
        if (ds == null) {
            synchronized (JDBCUtil.class){
                if (ds == null){
                    try{
                        Properties prop = new Properties();
                        InputStream stream = JDBCUtil.class.getClassLoader().getResourceAsStream("druid.properties");
                        prop.load(stream);
                        ds = DruidDataSourceFactory.createDataSource(prop);
                    }catch (IOException e){
                        throw new RuntimeException("druid properties error", e);
                    }catch (Exception e){
                        throw new RuntimeException("druid datasource error", e);
                    }
                }
            }
        }
    }

    public static Connection getConnection(){
        try{
            return ds.getConnection();
        }catch (SQLException e){
            throw new RuntimeException("get connection error", e);
        }
    }

    public static void close(ResultSet rs){
        if (rs == null){
            return;
        }
        try{
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
