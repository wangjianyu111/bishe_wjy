package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MonitorAlertResp {
    private Long alertId;
    private String alertLevel;
    private String alertLevelLabel;
    private String alertType;
    private String alertTypeLabel;
    private String alertTitle;
    private String alertContent;
    private String status;
    private String statusLabel;
    private LocalDateTime triggerTime;
    private LocalDateTime resolveTime;
    private Long resolvedBy;
    private String resolvedByName;
    private String resolveRemark;
    private LocalDateTime createTime;
}
