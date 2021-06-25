package com.vantop.apitest.user.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("global_department")
@Data
public class Department {
    @TableId
    private Integer id;
    private Integer platformId;
    private String departmentName;
    private Integer status;
    private Date updateTime;

}

