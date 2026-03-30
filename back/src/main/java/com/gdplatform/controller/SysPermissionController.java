package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.PermissionAddReq;
import com.gdplatform.dto.PermissionTreeNode;
import com.gdplatform.dto.PermissionUpdateReq;
import com.gdplatform.entity.SysPermission;
import com.gdplatform.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final PermissionService permissionService;

    @GetMapping("/page")
    public R<Page<SysPermission>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer permType) {
        return R.ok(permissionService.pagePermissions(current, size, keyword, permType));
    }

    @GetMapping("/tree")
    public R<List<PermissionTreeNode>> tree() {
        return R.ok(permissionService.getPermissionTree());
    }

    @GetMapping("/list")
    public R<List<SysPermission>> list() {
        return R.ok(permissionService.getPermissionList());
    }

    @GetMapping("/{permId}")
    public R<SysPermission> getById(@PathVariable Long permId) {
        return R.ok(permissionService.getPermissionList().stream()
                .filter(p -> p.getPermId().equals(permId))
                .findFirst()
                .orElse(null));
    }

    @PostMapping
    public R<SysPermission> add(@Valid @RequestBody PermissionAddReq req) {
        return R.ok(permissionService.add(req));
    }

    @PutMapping
    public R<SysPermission> update(@Valid @RequestBody PermissionUpdateReq req) {
        return R.ok(permissionService.update(req));
    }

    @DeleteMapping("/{permId}")
    public R<Void> delete(@PathVariable Long permId) {
        permissionService.delete(permId);
        return R.ok();
    }
}
