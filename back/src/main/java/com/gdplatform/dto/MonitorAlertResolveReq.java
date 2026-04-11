package com.gdplatform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonitorAlertResolveReq {
    private Long alertId;
    private String resolveRemark;
}
