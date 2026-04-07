package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampusUpdateReq {
    @NotNull(message = "校区ID不能为空")
    private Long campusId;
    @NotBlank(message = "校区名称不能为空")
    private String campusName;
    private String campusCode;
    private Integer sortOrder;
}
