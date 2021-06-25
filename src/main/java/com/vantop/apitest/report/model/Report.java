package com.vantop.apitest.report.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mingfeng_report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String taskId;
    private Integer caseType;
    private String labelName;
    private Integer executeId;
    private String kindName;
    private String tag;
    private Integer method;
    private String url;
    private String params;
    private String testAccount;
    private String response;
    private String errorLog;
    private String result;
    private String testUser;
    private Date testTime;
}

