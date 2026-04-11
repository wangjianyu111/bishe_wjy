package com.gdplatform.dto;

import lombok.Data;

@Data
public class ParamReq {
    private String type;
    private Integer status;
    private Integer current = 1;
    private Integer size = 20;
}
