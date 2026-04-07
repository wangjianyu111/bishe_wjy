package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectionActionReq {
    @NotNull(message = "申请ID不能为空")
    private Long selectionId;
    private String comment;
}
