package com.gdplatform.util;

import com.gdplatform.entity.SysUser;
import com.gdplatform.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser requireLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser lu)) {
            throw new IllegalStateException("未登录");
        }
        return lu;
    }

    public static SysUser currentUser() {
        return requireLoginUser().getUser();
    }
}
