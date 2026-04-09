package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProposalReviewReq {
    @NotNull(message = "开题报告ID不能为空")
    private Long proposalId;
    @NotNull(message = "审核结果不能为空")
    private String status;
    private String inspectComment;
}
