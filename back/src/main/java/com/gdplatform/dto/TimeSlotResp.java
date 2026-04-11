package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeSlotResp {
    private Long slotId;
    private String slotName;
    private Long schoolId;
    private String campusName;
    private String slotType;
    private String slotTypeLabel;
    private String startDate;
    private String endDate;
    private Integer slotOrder;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
}
