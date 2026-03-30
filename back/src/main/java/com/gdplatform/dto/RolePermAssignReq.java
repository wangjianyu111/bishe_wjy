package com.gdplatform.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolePermAssignReq {
    private Long roleId;
    private List<Long> permIds;
}
