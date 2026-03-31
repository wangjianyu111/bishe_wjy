package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordReq {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    private String newPassword;
}
