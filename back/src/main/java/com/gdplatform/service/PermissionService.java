package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.MenuNode;
import com.gdplatform.dto.PermissionAddReq;
import com.gdplatform.dto.PermissionTreeNode;
import com.gdplatform.dto.PermissionUpdateReq;
import com.gdplatform.entity.SysPermission;

import java.util.List;

public interface PermissionService {

    List<String> listPermCodes(Long userId);

    List<MenuNode> buildMenuTree(Long userId);

    Page<SysPermission> pagePermissions(long current, long size, String keyword, Integer permType);

    List<PermissionTreeNode> getPermissionTree();

    List<SysPermission> getPermissionList();

    SysPermission add(PermissionAddReq req);

    SysPermission update(PermissionUpdateReq req);

    void delete(Long permId);
}
