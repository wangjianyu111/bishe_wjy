package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long roleId;
    private String roleName;
    private String roleCode;
    private Integer userType;       // 该角色对应的账号类型：1学生 2教师 3管理员
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
