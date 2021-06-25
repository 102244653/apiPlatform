package com.vantop.apitest.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@TableName("global_dbconfig")

@Data
public class DBConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private Integer platform;
    private String ip;
    private String port;
    private String dbName;
    private String userName;
    private String passWord;
    private Integer status;
}
