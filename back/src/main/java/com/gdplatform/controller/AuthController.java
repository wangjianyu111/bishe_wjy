package com.gdplatform.controller;

import com.gdplatform.common.R;
import com.gdplatform.dto.EmailLoginRequest;
import com.gdplatform.dto.LoginRequest;
import com.gdplatform.dto.LoginResponse;
import com.gdplatform.dto.RegisterRequest;
import com.gdplatform.dto.SendVerificationCodeRequest;
import com.gdplatform.dto.UserProfile;
import com.gdplatform.entity.SysRole;
import com.gdplatform.mapper.SysRoleMapper;
import com.gdplatform.security.LoginUser;
import com.gdplatform.service.AuthService;
import com.gdplatform.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PermissionService permissionService;
    private final SysRoleMapper sysRoleMapper;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return R.ok();
    }
    
    @PostMapping("/send-verification-code")
    public R<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        authService.sendVerificationCode(request);
        return R.ok();
    }
    
    @PostMapping("/email-login")
    public R<LoginResponse> emailLogin(@Valid @RequestBody EmailLoginRequest request) {
        return R.ok(authService.emailLogin(request));
    }

    @GetMapping("/info")
    public R<LoginResponse> info(@AuthenticationPrincipal LoginUser loginUser) {
        var u = loginUser.getUser();
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(u.getUserId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();

        LoginResponse res = new LoginResponse();
        res.setUser(UserProfile.builder()
                .userId(u.getUserId())
                .userName(u.getUserName())
                .realName(u.getRealName())
                .userType(u.getUserType())
                .studentNo(u.getStudentNo())
                .teacherNo(u.getTeacherNo())
                .roles(roleCodes)
                .build());
        res.setMenus(permissionService.buildMenuTree(u.getUserId()));
        res.setPermissions(permissionService.listPermCodes(u.getUserId()));
        return R.ok(res);
    }
}
