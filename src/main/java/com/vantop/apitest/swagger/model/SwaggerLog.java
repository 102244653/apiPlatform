package com.vantop.apitest.swagger.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mingfeng_swagger_log")

public class SwaggerLog {
    @TableId(type = IdType.AUTO)
    private int id;
    private String kindName;
    private String name;
    private String method;
    private String record;
    private Date updateTime;
}

