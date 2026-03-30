package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectionApplyRequest {
    @NotNull(message = "课题不能为空")
    private Long topicId;
    @NotBlank(message = "学年不能为空")
    private String academicYear;
    private String applyReason;
}
