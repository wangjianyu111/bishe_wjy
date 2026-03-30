package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionUpdateReq {
    @NotNull(message = "权限ID不能为空")
    private Long permId;
    private Long parentId;
    @NotBlank(message = "权限名称不能为空")
    private String permName;
    private String permCode;
    @NotNull(message = "权限类型不能为空")
    private Integer permType;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Integer visible;
}
