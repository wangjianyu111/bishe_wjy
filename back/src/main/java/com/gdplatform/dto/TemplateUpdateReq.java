package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateUpdateReq {
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    private String phase;

    private String campusName;
}
