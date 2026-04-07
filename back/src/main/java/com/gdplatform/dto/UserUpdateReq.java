package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateReq {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String realName;
    private Integer userType;
    private String studentNo;
    private String teacherNo;
    private Long collegeId;
    private Long majorId;
    private Long campusId;
    private String campusName;
    private String userPhone;
    private String userEmail;
    private Integer status;
    private List<Long> roleIds;
}
