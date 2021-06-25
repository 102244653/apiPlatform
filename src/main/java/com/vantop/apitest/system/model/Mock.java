package com.vantop.apitest.system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mock {
    private int id;
    private String name;
    private String method;
    private String params;
}
