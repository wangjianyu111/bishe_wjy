package com.gdplatform.dto;

import lombok.Data;

@Data
public class PermissionTreeNode {
    private Long permId;
    private Long parentId;
    private String permName;
    private String permCode;
    private Integer permType;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Integer visible;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
    private java.util.List<PermissionTreeNode> children;
}
