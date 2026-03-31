package com.gdplatform.service;

import com.gdplatform.dto.ChangePasswordReq;
import com.gdplatform.dto.EmailLoginRequest;
import com.gdplatform.dto.LoginRequest;
import com.gdplatform.dto.LoginResponse;
import com.gdplatform.dto.RegisterRequest;
import com.gdplatform.dto.SendVerificationCodeRequest;
import com.gdplatform.dto.UserProfileUpdateReq;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void sendVerificationCode(SendVerificationCodeRequest request);

    LoginResponse emailLogin(EmailLoginRequest request);

    void changePassword(ChangePasswordReq request);

    void updateProfile(UserProfileUpdateReq req);
}
