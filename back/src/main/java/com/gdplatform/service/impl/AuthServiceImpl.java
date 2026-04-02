package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.ChangePasswordReq;
import com.gdplatform.dto.EmailLoginRequest;
import com.gdplatform.dto.LoginRequest;
import com.gdplatform.dto.LoginResponse;
import com.gdplatform.dto.RegisterRequest;
import com.gdplatform.dto.SendVerificationCodeRequest;
import com.gdplatform.dto.UserProfile;
import com.gdplatform.dto.UserProfileUpdateReq;
import com.gdplatform.entity.SysRole;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.SysRoleMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.security.JwtUtil;
import com.gdplatform.security.LoginUser;
import com.gdplatform.service.AuthService;
import com.gdplatform.service.MailService;
import com.gdplatform.service.PermissionService;
import com.gdplatform.util.VerificationCodeUtil;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final SysRoleMapper sysRoleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        SysUser u = loginUser.getUser();
        String token = jwtUtil.createToken(u.getUserId(), u.getUserName());

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(u.getUserId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();

        LoginResponse res = new LoginResponse();
        res.setToken(token);
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

        stringRedisTemplate.opsForValue().set(
                "gd:auth:lastLogin:" + u.getUserId(),
                String.valueOf(System.currentTimeMillis()),
                Duration.ofHours(24));
        return res;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BizException("两次输入的密码不一致");
        }

        SysUser existUser = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUserName, request.getUsername()));
        if (existUser != null) {
            throw new BizException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUserName(request.getUsername());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getUsername());
        user.setUserEmail(request.getEmail());
        user.setUserType(0); // 待分配，分配角色后自动同步
        user.setStatus(1);

        sysUserMapper.insert(user);
        sysUserMapper.batchInsertUserRole(user.getUserId(), List.of(4L)); // ROLE_PENDING
    }
    
    @Override
    public void sendVerificationCode(SendVerificationCodeRequest request) {
        // 生成验证码
        String code = VerificationCodeUtil.generateVerificationCode(6);

        // 将验证码存储到Redis，设置过期时间为5分钟
        stringRedisTemplate.opsForValue().set(
                "gd:verification:code:" + request.getEmail(),
                code,
                5, TimeUnit.MINUTES);

        // 发送验证码邮件
        mailService.sendVerificationCode(request.getEmail(), code);
    }
    
    @Override
    public LoginResponse emailLogin(EmailLoginRequest request) {
        // 从Redis获取验证码
        String storedCode = stringRedisTemplate.opsForValue()
                .get("gd:verification:code:" + request.getEmail());
        
        if (storedCode == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(request.getVerificationCode())) {
            throw new BizException("验证码错误");
        }
        
        // 根据邮箱查找用户，不存在则自动注册
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUserEmail, request.getEmail()));

        if (user == null) {
            // 自动创建账号，邮箱作为用户名
            user = new SysUser();
            user.setUserName(request.getEmail().split("@")[0] + "_" + System.currentTimeMillis() % 10000);
            user.setUserPassword(passwordEncoder.encode("123456"));
            user.setRealName(request.getEmail().split("@")[0]);
            user.setUserEmail(request.getEmail());
            user.setUserType(0); // 待分配，分配角色后自动同步
            user.setStatus(1);
            sysUserMapper.insert(user);
            sysUserMapper.batchInsertUserRole(user.getUserId(), List.of(4L)); // ROLE_PENDING
        }
        
        // 生成JWT令牌
        String token = jwtUtil.createToken(user.getUserId(), user.getUserName());
        
        // 获取用户角色
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getUserId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();

        LoginResponse res = new LoginResponse();
        res.setToken(token);
        res.setUser(UserProfile.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .studentNo(user.getStudentNo())
                .teacherNo(user.getTeacherNo())
                .roles(roleCodes)
                .build());
        res.setMenus(permissionService.buildMenuTree(user.getUserId()));
        res.setPermissions(permissionService.listPermCodes(user.getUserId()));

        stringRedisTemplate.opsForValue().set(
                "gd:auth:lastLogin:" + user.getUserId(),
                String.valueOf(System.currentTimeMillis()),
                Duration.ofHours(24));
        
        // 验证码使用后删除
        stringRedisTemplate.delete("gd:verification:code:" + request.getEmail());
        
        return res;
    }


    @Override
    public void changePassword(ChangePasswordReq request) {
        SysUser user = sysUserMapper.selectById(SecurityUtils.currentUser().getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getUserPassword())) {
            throw new BizException("原密码错误");
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 50) {
                throw new BizException("新密码长度为6-50位");
            }
            user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
            sysUserMapper.updateById(user);
        }
    }

    @Override
    public void updateProfile(UserProfileUpdateReq req) {
        Long userId = SecurityUtils.currentUser().getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (req.getRealName() != null) {
            user.setRealName(req.getRealName());
        }
        if (req.getStudentNo() != null) {
            user.setStudentNo(req.getStudentNo());
        }
        if (req.getTeacherNo() != null) {
            user.setTeacherNo(req.getTeacherNo());
        }
        if (req.getUserPhone() != null) {
            user.setUserPhone(req.getUserPhone());
        }
        if (req.getUserEmail() != null) {
            user.setUserEmail(req.getUserEmail());
        }
        sysUserMapper.updateById(user);
    }
}
