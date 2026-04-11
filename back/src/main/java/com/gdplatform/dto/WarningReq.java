package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarningReq {
    @NotNull(message = "选题ID不能为空")
    private Long selectionId;

    @NotNull(message = "预警级别不能为空")
    private Integer warnLevel;

    /** 预警类型: NO_GUIDANCE/DOC_DELAY/PROGRESS_DELAY/LOW_FREQUENCY/OTHER */
    private String warnType;

    @NotNull(message = "预警原因不能为空")
    private String reason;
}
