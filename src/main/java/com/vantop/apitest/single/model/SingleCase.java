package com.vantop.apitest.single.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.HashMap;

@Data
@TableName("mingfeng_singlecase")
public class SingleCase {
    @TableId(type = IdType.AUTO)
    private int id;
    private String name;
    private int method;
    private String params;
    private String tag;
    private Integer kindDirId;
    private String randKey="";
    private String beforeSql="";
    private String afterSql="";
    private String afterDBCheck="";
    private String assertion="";
    private Integer status=0;
    private Integer runStatus = 0;

    public HashMap<String, String> before(){
        HashMap<String, String> before = new HashMap<>();
        if(this.getRandKey().length()!=0){
            before.put("randKey", this.getRandKey());
        }
        if(this.getBeforeSql().length()!=0){
            before.put("beforeSql", this.getBeforeSql());
        }
        return before;
    }

    public HashMap<String, String> assertion(){
        HashMap<String, String> assertcase = new HashMap<>();
        if(this.getAssertion().length()!=0){
            assertcase.put("assertion", this.getAssertion());
        }
        if(this.getAfterDBCheck().length()!=0){
            assertcase.put("afterDBCheck", this.getAfterDBCheck());
        }
        return assertcase;
    }

    public HashMap<String, String> after(){
        HashMap<String, String> after = new HashMap<>();
        if(this.getAfterSql().length()!=0){
            after.put("afterSql", this.getAfterSql());
        }
        return after;
    }
}

