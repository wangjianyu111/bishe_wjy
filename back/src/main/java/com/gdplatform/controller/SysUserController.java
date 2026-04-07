package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.UserAddReq;
import com.gdplatform.dto.UserAssignRoleReq;
import com.gdplatform.dto.UserResp;
import com.gdplatform.dto.UserUpdateReq;
import com.gdplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final UserService userService;

    @GetMapping("/page")
    public R<Page<UserResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status) {
        return R.ok(userService.pageUsers(current, size, keyword, campusName, userType, status));
    }

    @GetMapping("/{userId}")
    public R<UserResp> getById(@PathVariable Long userId) {
        return R.ok(userService.getById(userId));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody UserAddReq req) {
        userService.add(req);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody UserUpdateReq req) {
        userService.update(req);
        return R.ok();
    }

    @DeleteMapping("/{userId}")
    public R<Void> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return R.ok();
    }

    @PostMapping("/assign-roles")
    public R<Void> assignRoles(@Valid @RequestBody UserAssignRoleReq req) {
        userService.assignRoles(req);
        return R.ok();
    }

    @PutMapping("/status/{userId}")
    public R<Void> toggleStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        userService.toggleStatus(userId, status);
        return R.ok();
    }
}
