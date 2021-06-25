package com.vantop.apitest.system.model;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mingfeng_dir")

public class Dir {
    @TableId
    private int kindId;
    private int kindDirId;
    private String dirName;
    private String kindName;
}
