package com.vantop.apitest.vo;

import lombok.Data;

import java.util.Date;

@Data
public class FlowLabelVO {

    private Integer id;
    private int kindId;
    private String kindName;
    private String labelName;
    private String labelDescription;
    private Integer accountId;
    private String account;
    private Integer status;
    private Long userId;
    private String owner;
    private Integer isOpen;
    private String createTime;
}
