package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.PermissionTreeNode;
import com.gdplatform.dto.RoleAddReq;
import com.gdplatform.dto.RolePermAssignReq;
import com.gdplatform.dto.RoleResp;
import com.gdplatform.dto.RoleUpdateReq;
import com.gdplatform.service.PermissionService;
import com.gdplatform.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    @GetMapping("/page")
    public R<Page<RoleResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return R.ok(roleService.pageRoles(current, size, keyword));
    }

    @GetMapping
    public R<List<RoleResp>> list() {
        return R.ok(roleService.listAll());
    }

    @GetMapping("/{roleId}")
    public R<RoleResp> getById(@PathVariable Long roleId) {
        return R.ok(roleService.getById(roleId));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody RoleAddReq req) {
        roleService.add(req);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody RoleUpdateReq req) {
        roleService.update(req, req.getRoleId());
        return R.ok();
    }

    @DeleteMapping("/{roleId}")
    public R<Void> delete(@PathVariable Long roleId) {
        roleService.delete(roleId);
        return R.ok();
    }

    @GetMapping("/permissions/tree")
    public R<List<PermissionTreeNode>> permissionTree() {
        return R.ok(permissionService.getPermissionTree());
    }

    @PostMapping("/assign-permissions")
    public R<Void> assignPermissions(@RequestBody RolePermAssignReq req) {
        roleService.assignPermissions(req);
        return R.ok();
    }
}
