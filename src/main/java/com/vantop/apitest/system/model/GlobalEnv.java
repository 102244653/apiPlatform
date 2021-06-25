package com.vantop.apitest.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("global_env")
public class GlobalEnv {
    @TableId(type = IdType.AUTO)
    private int id;
    private int platForm;
    private String urlPre;
    private String remoteAddress;
    private String name;
    private Long uid;
    private int status;
}

