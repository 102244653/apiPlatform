package com.vantop.apitest.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("global_test_account")
public class TestAccount {
    @TableId(type = IdType.AUTO)
    private int id;
    private Long uid;
    private int platform;
    private String account;
    private String password;
    private String loginDetail="";
    private String cookie="";
    private int isDefault=0;
}