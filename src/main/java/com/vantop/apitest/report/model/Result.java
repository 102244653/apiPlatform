package com.vantop.apitest.report.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mingfeng_result")
public class Result {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String taskId;
    private Integer taskType;
    //    private String env;
    private Integer totalLabel=0;
    private Integer labelPass=0;
    private Integer labelFail=0;
    private Integer totalCase=0;
    private Integer casePass=0;
    private Integer caseFail=0;
    private Integer runStatus;
    private String logs;
    private String testUser;
    private Date testTime;
}

