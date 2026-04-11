package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TimeSlotUpdateReq {

    @NotNull(message = "ID不能为空")
    private Long slotId;

    private String slotName;
    private Long schoolId;
    private String slotType;
    private String startDate;
    private String endDate;
    private Integer slotOrder;
    private Integer status;
}
