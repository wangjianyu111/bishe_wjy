package com.gdplatform.dto;

import com.gdplatform.entity.SysRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleResp {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Long> permIds;
}
