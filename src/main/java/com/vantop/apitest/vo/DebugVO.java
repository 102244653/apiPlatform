package com.vantop.apitest.vo;

import lombok.Data;

@Data
public class DebugVO {
    private String result="fail";
    private String address="";
    private String request="";
    private String response="";
    private String errorLog="";
}
