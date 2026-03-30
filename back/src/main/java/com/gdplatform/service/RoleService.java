package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.RoleAddReq;
import com.gdplatform.dto.RolePermAssignReq;
import com.gdplatform.dto.RoleResp;
import com.gdplatform.dto.RoleUpdateReq;

import java.util.List;

public interface RoleService {

    Page<RoleResp> pageRoles(long current, long size, String keyword);

    List<RoleResp> listAll();

    RoleResp getById(Long roleId);

    void add(RoleAddReq req);

    void update(RoleUpdateReq req, Long roleId);

    void delete(Long roleId);

    void assignPermissions(RolePermAssignReq req);
}
