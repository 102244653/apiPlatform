package com.vantop.apitest.system.model;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("global_rule")
public class GlobalRule {
    @TableId
    private Integer id;
    private String action;
    private String description;
    private String rule;
    private String detail;
}
