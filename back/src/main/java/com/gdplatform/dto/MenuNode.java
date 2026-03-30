package com.gdplatform.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuNode {
    private Long permId;
    private Long parentId;
    private String permName;
    private String permCode;
    private Integer permType;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private List<MenuNode> children = new ArrayList<>();
}
