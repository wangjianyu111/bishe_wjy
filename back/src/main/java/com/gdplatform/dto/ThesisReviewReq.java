package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThesisReviewReq {
    @NotNull(message = "论文ID不能为空")
    private Long thesisId;
    @NotNull(message = "审核结果不能为空")
    private String status;
    private String inspectComment;
}