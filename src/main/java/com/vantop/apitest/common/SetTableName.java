package com.vantop.apitest.common;


import java.util.ArrayList;
import java.util.List;

public class SetTableName {

    public static String setTable(Integer platform, String table){
        String pf = "";
        if(platform == 1){
            pf="mingfeng";
        }else if(platform==2){
            pf="community";
        }
        String tableName=table;
        switch (table){
            case "swagger":
                tableName = pf+"_"+table ;
                break;
            case "swagger_log":
                tableName = pf+"_"+table ;
                break;
            case "singlereport":
                tableName = pf+"_"+table ;
                break;
            case "singlecase":
                tableName = pf+"_"+table ;
                break;
            case "flowreport":
                tableName = pf+"_"+table ;
                break;
            case "flowcase":
                tableName = pf+"_"+table ;
                break;
            case "flowlabel":
                tableName = pf+"_"+table ;
                break;
            case "dir":
                tableName = pf+"_"+table ;
                break;
            case "config_kind":
                tableName = pf+"_"+table ;
                break;
            case "blacklist":
                tableName = pf+"_"+table ;
                break;
        }
        return tableName;
    }

    /**
     * 动态表名list
     * */
    public static List<String> getTableName(){
        List<String> table = new ArrayList<>();
        table.add("mingfeng_test_account");
        table.add("mingfeng_config_kind");
        table.add("mingfeng_blacklist");
        table.add("mingfeng_dir");
        table.add("mingfeng_flowcase");
        table.add("mingfeng_flowreport");
        table.add("mingfeng_singlecase");
        table.add("mingfeng_singlereport");
        table.add("mingfeng_swagger");
        table.add("mingfeng_swagger_log");

        return table;
    }
}
