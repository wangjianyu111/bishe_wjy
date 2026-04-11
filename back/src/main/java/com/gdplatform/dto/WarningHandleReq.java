package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarningHandleReq {
    @NotNull(message = "预警ID不能为空")
    private Long warnId;

    private String handleComment;
}
