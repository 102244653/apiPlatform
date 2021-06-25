package com.vantop.apitest.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mingfeng_config_kind")
public class ConfigKind {
    @TableId(type = IdType.AUTO)
    private int kindId;
    private String kindName;
    private String swaggerUrl;
    private String base;
    private int status;
}
