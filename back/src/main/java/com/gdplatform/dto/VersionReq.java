package com.gdplatform.dto;

import lombok.Data;

@Data
public class VersionReq {
    private String appType;
    private String status;
    private String keyword;
    private Integer current = 1;
    private Integer size = 20;
}
