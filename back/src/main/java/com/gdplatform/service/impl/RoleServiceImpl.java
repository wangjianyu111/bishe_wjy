package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.RoleAddReq;
import com.gdplatform.dto.RolePermAssignReq;
import com.gdplatform.dto.RoleResp;
import com.gdplatform.dto.RoleUpdateReq;
import com.gdplatform.entity.SysRole;
import com.gdplatform.mapper.SysRoleMapper;
import com.gdplatform.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public Page<RoleResp> pageRoles(long current, long size, String keyword) {
        Page<RoleResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<RoleResp> records = sysRoleMapper.selectRolePage(keyword, offset, size);
        long total = sysRoleMapper.countRolePage(keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<RoleResp> listAll() {
        List<SysRole> roles = sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery().orderByDesc(SysRole::getCreateTime));
        return roles.stream().map(this::toResp).toList();
    }

    @Override
    public RoleResp getById(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        RoleResp resp = toResp(role);
        resp.setPermIds(sysRoleMapper.selectPermIdsByRoleId(roleId));
        return resp;
    }

    @Override
    @Transactional
    public void add(RoleAddReq req) {
        SysRole exist = sysRoleMapper.selectByRoleCode(req.getRoleCode());
        if (exist != null) {
            throw new BizException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleName(req.getRoleName());
        role.setRoleCode(req.getRoleCode());
        role.setUserType(req.getUserType());
        role.setRemark(req.getRemark());
        sysRoleMapper.insert(role);
    }

    @Override
    @Transactional
    public void update(RoleUpdateReq req, Long roleId) {
        SysRole role = sysRoleMapper.selectById(req.getRoleId());
        if (role == null) {
            throw new BizException("角色不存在");
        }
        SysRole exist = sysRoleMapper.selectOne(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getRoleCode, req.getRoleCode())
                        .ne(SysRole::getRoleId, roleId));
        if (exist != null) {
            throw new BizException("角色编码已存在");
        }
        role.setRoleName(req.getRoleName());
        role.setRoleCode(req.getRoleCode());
        role.setUserType(req.getUserType());
        role.setRemark(req.getRemark());
        sysRoleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void delete(Long roleId) {
        sysRoleMapper.deleteById(roleId);
    }

    @Override
    @Transactional
    public void assignPermissions(RolePermAssignReq req) {
        sysRoleMapper.deleteRolePermissionByRoleId(req.getRoleId());
        if (req.getPermIds() != null && !req.getPermIds().isEmpty()) {
            sysRoleMapper.batchInsertRolePermission(req.getRoleId(), req.getPermIds());
        }
    }

    private RoleResp toResp(SysRole role) {
        RoleResp resp = new RoleResp();
        resp.setRoleId(role.getRoleId());
        resp.setRoleName(role.getRoleName());
        resp.setRoleCode(role.getRoleCode());
        resp.setUserType(role.getUserType());
        resp.setRemark(role.getRemark());
        resp.setCreateTime(role.getCreateTime());
        resp.setUpdateTime(role.getUpdateTime());
        return resp;
    }
}
