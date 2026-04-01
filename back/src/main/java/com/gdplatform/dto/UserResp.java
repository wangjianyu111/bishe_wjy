package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResp {
    private Long userId;
    private String userName;
    private String realName;
    private Integer userType;
    private String studentNo;
    private String teacherNo;
    private Long collegeId;
    private Long majorId;
    private String userAvatar;
    private String userPhone;
    private String userEmail;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Long> roleIds;
    private String roleNames; // GROUP_CONCAT 拼出的角色名，如"管理员,学生"
}
