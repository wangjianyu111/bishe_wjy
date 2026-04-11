package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParamUpdateReq {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String code;

    private Long schoolId;

    private Integer status;

    private Integer sortOrder;
}
