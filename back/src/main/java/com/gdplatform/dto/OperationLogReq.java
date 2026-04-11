package com.gdplatform.dto;

import lombok.Data;

@Data
public class OperationLogReq {
    private String operationType;
    private String operationName;
    private String keyword;
    private String startDate;
    private String endDate;
    private Integer current = 1;
    private Integer size = 20;
}
