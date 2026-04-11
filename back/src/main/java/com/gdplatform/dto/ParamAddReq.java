package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParamAddReq {

    @NotBlank(message = "名称不能为空")
    private String name;

    private String code;

    @NotNull(message = "类型不能为空")
    private String type;

    private Long schoolId;

    private Integer status = 1;

    private Integer sortOrder;
}
