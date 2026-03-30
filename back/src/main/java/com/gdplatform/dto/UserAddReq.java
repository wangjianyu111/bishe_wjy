package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserAddReq {
    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String userPassword;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    private String studentNo;
    private String teacherNo;
    private Long collegeId;
    private Long majorId;
    private String userPhone;
    private String userEmail;
    private Integer status;
    private List<Long> roleIds;
}
