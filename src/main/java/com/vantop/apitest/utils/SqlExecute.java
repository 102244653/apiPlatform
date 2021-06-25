package com.vantop.apitest.utils;

import com.vantop.apitest.system.model.DBConfig;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j

public class SqlExecute {

    private Connection conn = null;
    private Statement stmt = null;

    //驱动（8.0+）
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";


    public SqlExecute(DBConfig dbConfig) {
        log.info("连接数据库["+dbConfig.getTitle()+"]...");
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
            //创建链接
            String DB_URL = "jdbc:mysql://"+dbConfig.getIp()+":"+dbConfig.getPort()+"/"+dbConfig.getDbName()+"?useSSL=" +
                    "false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            conn = DriverManager.getConnection(DB_URL,dbConfig.getUserName(),dbConfig.getPassWord());
            //实例化Statement对象...
            stmt = conn.createStatement();
            log.info("连接数据库["+dbConfig.getTitle()+"]成功！");
        }catch(SQLException se){
            log.info("连接数据库["+dbConfig.getTitle()+"]失败！");
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            log.info("连接数据库["+dbConfig.getTitle()+"]失败！");
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (conn == null && stmt == null) {
            return;
        }
        // 关闭资源
        try{
            if(stmt!=null) stmt.close();
        }catch(SQLException se2){
            se2.printStackTrace();
        }
        try{
            if(conn!=null) conn.close();
        }catch(SQLException se){
            se.printStackTrace();
        }
    }

    //查询并返回多行数据
//    private ArrayList<HashMap<String, Object>> query(String sql) {
//        //最终返回结果List
//        ArrayList<HashMap<String, Object>> all = new ArrayList<>();
//        if(conn==null || stmt == null){
//            log.error("数据库链接失败...");
//            return all;
//        }
//
//        try {
//            ResultSet rs = stmt.executeQuery(sql);
//            //获取数据字段信息
//            ResultSetMetaData data = rs.getMetaData();
//
//            // 展开结果集数据库
//            while (rs.next()) {
//                //每行数据为一个map
//                HashMap<String, Object> map = new HashMap<>();
//                // 数据库里从 1 开始
//                for (int i = 1; i <= data.getColumnCount(); i++) {
//                    String c = data.getColumnName(i);
//                    switch (data.getColumnTypeName(i)){
//                        case "INT":
//                            map.put(c, rs.getInt(c));
//                            break;
//                        case "VARCHAR":
//                            map.put(c, rs.getString(c));
//                            break;
//                        case "BIGINT":
//                            map.put(c, rs.getLong(c));
//                            break;
//                        case "DATE":
//                            map.put(c, rs.getDate(c));
//                            break;
//                    }
//                    log.info("读取值@"+c + "=" + map.get(c)+"&类型:"+map.get(c).getClass().getName());
//                }
//                all.add(map);
//            }
//            // 完成后关闭
//            rs.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return all;
//    }

    //查询返回一行数据
    private HashMap<String, Object> query(String sql) {
        //最终返回结果Map
        HashMap<String, Object> all = new HashMap<>();
        if(conn==null || stmt == null){
            log.error("数据库链接失败...");
            return all;
        }

        try {
            ResultSet rs = stmt.executeQuery(sql);
            //获取数据字段信息
            ResultSetMetaData data = rs.getMetaData();
            // 展开结果集数据库
            while (rs.next()) {
                // 数据库里从 1 开始
                for (int i = 1; i <= data.getColumnCount(); i++) {
                    String c = data.getColumnName(i);
                    switch (data.getColumnTypeName(i)){
                        case "INT":
                            all.put(c, rs.getInt(c));
                            break;
                        case "VARCHAR":
                            all.put(c, rs.getString(c));
                            break;
                        case "BIGINT":
                            all.put(c, rs.getLong(c));
                            break;
                        case "DATE":
                            all.put(c, rs.getDate(c));
                            break;
                    }
//                    log.info("读取值@"+c + "=" + all.get(c)+"&类型:"+all.get(c).getClass().getName());
                }
                break;
            }
            // 完成后关闭
            rs.close();
            all.put("sqlResult", "pass");
        }catch (Exception e){
            e.printStackTrace();
            all.put("sqlResult", "fail");
        }
        log.info("QUERY-RESULT:\n"+all.toString());
        return all;
    }

    //插入、更新、删除
    private boolean update(String sql){
        if(conn==null || stmt == null){
            log.error("数据库链接失败...");
        }
        try {
            int rs = stmt.executeUpdate(sql);
            log.info("UPDATE-RESULT:"+rs);
            //处理结果
            if(rs>0){
                return true;
            }else{
                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public HashMap<String, Object> runSql(String mysql) {
        HashMap<String, Object> resultMap = new HashMap<>();
        if(mysql.trim().length()==0){
            resultMap.put("sqlResult", "fail");
            return resultMap;
        }
        if(mysql.trim().startsWith("S")||mysql.trim().startsWith("s")){
            resultMap = this.query(mysql);
        }else {
            if(this.update(mysql)){
                resultMap.put("sqlResult", "pass");
            }else {
                resultMap.put("sqlResult", "fail");
            }
        }
        return resultMap;
    }


}
