package com.gdplatform.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorApiStatsResp {
    private Long statId;
    private String apiPath;
    private String apiName;
    private String requestMethod;
    private BigDecimal avgResponseTime;
    private Long maxResponseTime;
    private Long minResponseTime;
    private Long totalRequests;
    private Long successCount;
    private Long failCount;
    private BigDecimal tps;
    private LocalDateTime statTime;
}
