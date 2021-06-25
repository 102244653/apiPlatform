package com.vantop.apitest.vo;

import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.user.model.TestAccount;
import lombok.Data;

import java.util.HashMap;

@Data
public class Case {
    private int id; //用例id
    private String taskId;//任务id,统计报告
    private Integer labelId=0;//标签id
    private String labelName="";//标签名称
    private TestAccount testUser;//测试账号
    private Integer kindId;//二级耳机模块id
    private String kindName="";//模块名称
    private Integer kindDirId;//二级耳机模块id
    private String kindDirName="";//二级耳机模块名称
    private Integer caseType;//用例类型
    private String tag="";//用力名
    private Integer executeId=0;//执行顺序
    private GlobalEnv globalEnv;//测试环境
    private String name="";//接口地址
    private int method;//请求方法
    private String params="";//请求参数
    private HashMap<String, String> before;//前置操作:用例依赖的处理、随机数、数据库处理等
    private String response="";//响应结果
    private String errorLog="";//错误信息
    private HashMap<String, String> assertion;//断言操作：断言（数据库断言、res断言）
    private HashMap<String, String> after;//后置操作：数据库恢复、接口恢复
    private String result;//用例执行结果
}

