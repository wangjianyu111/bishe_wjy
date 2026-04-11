package com.gdplatform.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorMetricsResp {
    private Long metricId;
    private String metricType;
    private String metricTypeLabel;
    private String metricName;
    private BigDecimal metricValue;
    private String metricUnit;
    private String status;
    private String statusLabel;
    private BigDecimal threshold;
    private LocalDateTime recordTime;
    private LocalDateTime createTime;
}
