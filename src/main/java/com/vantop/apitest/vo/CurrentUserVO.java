package com.vantop.apitest.vo;

import lombok.Data;

@Data
public class CurrentUserVO {
    private Long uid;
    private String userName;
    private Integer platform;
    private Integer department;
    private String icon;
    private Integer status;
    private String token;

}
