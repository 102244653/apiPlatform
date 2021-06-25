package com.vantop.apitest.single.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mingfeng_singlereport")
public class SingleReport {
    @TableId
    private String id;
    private String taskId;
    private String name;
    private String url;
    private String tag;
    private int success;
    private String result;
    private String req;
    private String res;
    private Long uid;
    private String runner;
    private Integer test_account_id;
    private String test_account_name;
    private Date create_time;
}

