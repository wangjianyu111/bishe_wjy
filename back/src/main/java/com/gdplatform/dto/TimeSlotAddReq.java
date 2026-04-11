package com.gdplatform.dto;

import lombok.Data;

@Data
public class TimeSlotAddReq {
    private String slotName;
    private Long schoolId;
    private String slotType;
    private String startDate;
    private String endDate;
    private Integer slotOrder;
    private Integer status = 1;
}
