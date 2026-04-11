package com.gdplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationLogResp {
    private Long logId;
    private String operationType;
    private String operationName;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private Long userId;
    private String userName;
    private Integer userType;
    private String campusName;
    private String ipAddress;
    private String ipLocation;
    private String os;
    private String browser;
    private Long duration;
    private String module;
    private String status;
    private String errorMsg;
    private String operateTime;
}
