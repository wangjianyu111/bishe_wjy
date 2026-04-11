package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GuidanceRelationHandleReq {

    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    @NotBlank(message = "处理结果不能为空")
    private String status;
}
