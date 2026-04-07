package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CampusAddReq {
    @NotBlank(message = "校区名称不能为空")
    private String campusName;
    private String campusCode;
    private Integer sortOrder;
}
