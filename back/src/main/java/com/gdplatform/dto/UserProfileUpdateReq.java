package com.gdplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileUpdateReq {

    private String realName;

    private String studentNo;

    private String teacherNo;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String userPhone;

    @Email(message = "请输入正确的邮箱地址")
    private String userEmail;
}
