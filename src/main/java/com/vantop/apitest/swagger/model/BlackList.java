package com.vantop.apitest.swagger.model;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mingfeng_blacklist")

public class BlackList {
    private String name;
    private Integer kind;
    private String method;
    private Long uid;
    private Date updateTime;
}
