package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateAddReq {
    @NotBlank(message = "毕设阶段不能为空")
    private String phase;

    @NotBlank(message = "学校名称不能为空")
    private String campusName;
}
