package com.vantop.apitest.flow.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mingfeng_flowlabel")

public class FlowLabel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer kindId;
    private String labelName;
    private String labelDescription;
    private Integer accountId;
    private Integer status;
    private Long userId;
    private Integer isOpen;
    private Date createTime;
}
