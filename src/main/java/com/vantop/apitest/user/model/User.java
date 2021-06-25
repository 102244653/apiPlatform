package com.vantop.apitest.user.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vantop.apitest.utils.DESUtil;
import lombok.Data;

import java.util.Date;

@Data
@TableName("global_user")
public class User {
    @TableId
    private Long uid;
    private String userName;
    private String phone;
    private String email;
    private String passWord;
    private Integer department;
    private Integer platform;
    private Integer status;
    private String icon;
    private Date createTime;
    private String loginTime;//最后登陆时间

    public void resetLoginTime(){
        this.setLoginTime(new DESUtil().getTimeStamp());
    }

    public void resetPassWord(){
        this.setPassWord(new DESUtil().encryptPassword(this.getPassWord()));
    }
}
