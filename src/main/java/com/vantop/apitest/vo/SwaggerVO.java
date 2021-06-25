package com.vantop.apitest.vo;

import lombok.Data;

import java.util.Date;

@Data
public class SwaggerVO {
    private int id;
    private int kind;
    private String kindName;
    private String name;
    private String method;
    private String tag;
    private int status;
    private String updateTime;

    private String header = "";
    private String parameters;
    private String params = "";
    private String responses = "";
}
