package com.vantop.apitest.swagger.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@TableName("mingfeng_swagger")
public class Swagger {
    @TableId(type = IdType.AUTO)
    private int id;
    private int kind;
    private String name;
    private String method;
    private String tag;
    private String header = "";
    private String parameters;
    private String params = "";
    private String responses = "";
    private int status;
    private Date updateTime;

    public void SetParams(){
        this.params = this.parameters;
    }

}
