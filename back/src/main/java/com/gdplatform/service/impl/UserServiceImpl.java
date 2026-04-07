package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.UserAddReq;
import com.gdplatform.dto.UserAssignRoleReq;
import com.gdplatform.dto.UserResp;
import com.gdplatform.dto.UserUpdateReq;
import com.gdplatform.entity.SysCampus;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.CampusMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.CampusService;
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
    private final CampusMapper campusMapper;
    private final CampusService campusService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResp> pageUsers(long current, long size, String keyword, String campusName, Integer userType, Integer status) {
        Page<UserResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<UserResp> records = sysUserMapper.selectUserPage(keyword, campusName, userType, status, offset, size);
        long total = sysUserMapper.countUserPage(keyword, campusName, userType, status);

        for (UserResp resp : records) {
            resp.setRoleIds(sysUserMapper.selectRoleIdsByUserId(resp.getUserId()));
            // roleNames 已由 GROUP_CONCAT 直接返回，无需再查
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
        UserResp resp = toResp(user);
        if (user.getCampusId() != null) {
            SysCampus campus = campusMapper.selectById(user.getCampusId());
            if (campus != null) {
                resp.setCampusName(campus.getCampusName());
            }
        }
        return resp;
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
        if (req.getCampusName() != null && !req.getCampusName().isBlank()) {
            user.setCampusId(campusService.findOrCreateByName(req.getCampusName()));
        } else if (req.getCampusId() != null) {
            user.setCampusId(req.getCampusId());
        }
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
        if (req.getCampusId() != null) {
            user.setCampusId(req.getCampusId());
        }
        if (req.getCampusName() != null && !req.getCampusName().isBlank()) {
            user.setCampusId(campusService.findOrCreateByName(req.getCampusName()));
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
                // 根据角色表中配置的 userType 自动更新用户的账号类型
                List<Integer> roleUserTypes = sysUserMapper.selectUserTypesByUserIdAndRoles(req.getUserId(), req.getRoleIds());
                user.setUserType(deriveUserTypeFromRoles(roleUserTypes));
            }
        }
        sysUserMapper.updateById(user);
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
            // 根据角色表中配置的 userType 自动更新用户的账号类型
            List<Integer> roleUserTypes = sysUserMapper.selectUserTypesByUserIdAndRoles(req.getUserId(), req.getRoleIds());
            Integer newUserType = deriveUserTypeFromRoles(roleUserTypes);
            user.setUserType(newUserType);
            sysUserMapper.updateById(user);
        }
    }

    @Override
    public Integer deriveUserTypeFromRoles(List<Integer> roleUserTypes) {
        if (roleUserTypes == null || roleUserTypes.isEmpty()) {
            return 0; // 待分配
        }
        // 过滤掉 NULL，只保留有效的 userType
        List<Integer> validTypes = roleUserTypes.stream()
                .filter(t -> t != null)
                .toList();
        if (validTypes.isEmpty()) {
            return 0; // 所有角色都没有配置 userType，视为待分配
        }
        // 优先级：管理员 > 教师 > 学生
        if (validTypes.contains(3)) {
            return 3;
        }
        if (validTypes.contains(2)) {
            return 2;
        }
        return 1;
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
        resp.setCampusId(user.getCampusId());
        resp.setCampusName(null); // 由 Mapper XML JOIN 查出，单独查单条时通过 college 间接获取
        resp.setUserAvatar(user.getUserAvatar());
        resp.setUserPhone(user.getUserPhone());
        resp.setUserEmail(user.getUserEmail());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        resp.setUpdateTime(user.getUpdateTime());
        resp.setRoleIds(sysUserMapper.selectRoleIdsByUserId(user.getUserId()));
        List<String> names = sysUserMapper.selectRoleNamesByUserId(user.getUserId());
        resp.setRoleNames(names == null || names.isEmpty() ? null : String.join(",", names));
        return resp;
    }
}
