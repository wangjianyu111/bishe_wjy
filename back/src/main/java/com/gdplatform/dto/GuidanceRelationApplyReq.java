package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GuidanceRelationApplyReq {

    @NotNull(message = "目标用户ID不能为空")
    private Long toUserId;

    private String message;

    private String academicYear;
}
