package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.UserAddReq;
import com.gdplatform.dto.UserAssignRoleReq;
import com.gdplatform.dto.UserResp;
import com.gdplatform.dto.UserUpdateReq;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResp> pageUsers(long current, long size, String keyword) {
        Page<UserResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<UserResp> records = sysUserMapper.selectUserPage(keyword, offset, size);
        long total = sysUserMapper.countUserPage(keyword);

        for (UserResp resp : records) {
            resp.setRoleIds(sysUserMapper.selectRoleIdsByUserId(resp.getUserId()));
        }

        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public UserResp getById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return toResp(user);
    }

    @Override
    @Transactional
    public void add(UserAddReq req) {
        if (sysUserMapper.existsByUserName(req.getUserName())) {
            throw new BizException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUserName(req.getUserName());
        user.setUserPassword(passwordEncoder.encode(req.getUserPassword()));
        user.setRealName(req.getRealName());
        user.setUserType(req.getUserType());
        user.setStudentNo(req.getStudentNo());
        user.setTeacherNo(req.getTeacherNo());
        user.setCollegeId(req.getCollegeId());
        user.setMajorId(req.getMajorId());
        user.setUserPhone(req.getUserPhone());
        user.setUserEmail(req.getUserEmail());
        user.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        sysUserMapper.insert(user);

        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            sysUserMapper.batchInsertUserRole(user.getUserId(), req.getRoleIds());
        }
    }

    @Override
    @Transactional
    public void update(UserUpdateReq req) {
        SysUser user = sysUserMapper.selectById(req.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (sysUserMapper.isAdminByUserId(req.getUserId()) && req.getStatus() != null && req.getStatus() == 0) {
            throw new BizException("管理员角色的用户不能被禁用");
        }

        if (req.getRealName() != null) {
            user.setRealName(req.getRealName());
        }
        if (req.getUserType() != null) {
            user.setUserType(req.getUserType());
        }
        if (req.getStudentNo() != null) {
            user.setStudentNo(req.getStudentNo());
        }
        if (req.getTeacherNo() != null) {
            user.setTeacherNo(req.getTeacherNo());
        }
        if (req.getCollegeId() != null) {
            user.setCollegeId(req.getCollegeId());
        }
        if (req.getMajorId() != null) {
            user.setMajorId(req.getMajorId());
        }
        if (req.getUserPhone() != null) {
            user.setUserPhone(req.getUserPhone());
        }
        if (req.getUserEmail() != null) {
            user.setUserEmail(req.getUserEmail());
        }
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        sysUserMapper.updateById(user);

        if (req.getRoleIds() != null) {
            sysUserMapper.deleteUserRoleByUserId(req.getUserId());
            if (!req.getRoleIds().isEmpty()) {
                sysUserMapper.batchInsertUserRole(req.getUserId(), req.getRoleIds());
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (sysUserMapper.isAdminByUserId(userId)) {
            throw new BizException("管理员角色的用户不能删除");
        }
        sysUserMapper.deleteById(userId);
    }

    @Override
    @Transactional
    public void assignRoles(UserAssignRoleReq req) {
        SysUser user = sysUserMapper.selectById(req.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        sysUserMapper.deleteUserRoleByUserId(req.getUserId());
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            sysUserMapper.batchInsertUserRole(req.getUserId(), req.getRoleIds());
        }
    }

    @Override
    @Transactional
    public void toggleStatus(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (sysUserMapper.isAdminByUserId(userId) && status == 0) {
            throw new BizException("管理员角色的用户不能被禁用");
        }

        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    private UserResp toResp(SysUser user) {
        UserResp resp = new UserResp();
        resp.setUserId(user.getUserId());
        resp.setUserName(user.getUserName());
        resp.setRealName(user.getRealName());
        resp.setUserType(user.getUserType());
        resp.setStudentNo(user.getStudentNo());
        resp.setTeacherNo(user.getTeacherNo());
        resp.setCollegeId(user.getCollegeId());
        resp.setMajorId(user.getMajorId());
        resp.setUserAvatar(user.getUserAvatar());
        resp.setUserPhone(user.getUserPhone());
        resp.setUserEmail(user.getUserEmail());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        resp.setUpdateTime(user.getUpdateTime());
        resp.setRoleIds(sysUserMapper.selectRoleIdsByUserId(user.getUserId()));
        return resp;
    }
}
