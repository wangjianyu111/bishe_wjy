package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.UserAddReq;
import com.gdplatform.dto.UserAssignRoleReq;
import com.gdplatform.dto.UserResp;
import com.gdplatform.dto.UserUpdateReq;

public interface UserService {

    Page<UserResp> pageUsers(long current, long size, String keyword);

    UserResp getById(Long userId);

    void add(UserAddReq req);

    void update(UserUpdateReq req);

    void delete(Long userId);

    void assignRoles(UserAssignRoleReq req);

    void toggleStatus(Long userId, Integer status);
}
