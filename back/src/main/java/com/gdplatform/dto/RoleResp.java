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
    private Integer userType;       // 该角色对应的账号类型
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Long> permIds;
}
