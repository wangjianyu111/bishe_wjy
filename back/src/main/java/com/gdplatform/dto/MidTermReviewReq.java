package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MidTermReviewReq {
    @NotNull(message = "中期检查ID不能为空")
    private Long midId;

    @NotNull(message = "审核结果不能为空")
    private String status;

    private String inspectComment;
}
