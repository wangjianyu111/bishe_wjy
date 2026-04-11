package com.gdplatform.dto;

import lombok.Data;

@Data
public class MonitorReq {
    private String metricType;
    private String status;
    private String alertLevel;
    private String startDate;
    private String endDate;
    private Integer current = 1;
    private Integer size = 20;
}
