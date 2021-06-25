package com.vantop.apitest.flow.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.HashMap;

@Data
@TableName("mingfeng_flowcase")

public class FlowCase {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer labelId;
    private String tag;
    private String labelName;
    private String name;
    private Integer method;
    private Integer kindId;

    @TableField(exist = false)
    private String kindName="";

    private Integer executeId;

    private String params;

    private String depend="";
    private String randKey="";
    private String beforeSql="";

    private String response="";

    private String assertion="";
    private String afterSql="";
    private String afterDBCheck="";

    private Integer status;

    public HashMap<String, String> before(){
        HashMap<String, String> before = new HashMap<>();
        if(this.getDepend().length()!=0){
            before.put("depend", this.getDepend());
        }
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
