package com.gdplatform.service;

import com.gdplatform.dto.EmailLoginRequest;
import com.gdplatform.dto.LoginRequest;
import com.gdplatform.dto.LoginResponse;
import com.gdplatform.dto.RegisterRequest;
import com.gdplatform.dto.SendVerificationCodeRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
    
    void sendVerificationCode(SendVerificationCodeRequest request);
    
    LoginResponse emailLogin(EmailLoginRequest request);
}
