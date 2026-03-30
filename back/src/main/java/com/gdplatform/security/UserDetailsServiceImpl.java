package com.gdplatform.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.SysPermissionMapper;
import com.gdplatform.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectActiveUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<String> codes = sysPermissionMapper.selectPermCodesByUserId(user.getUserId());
        List<SimpleGrantedAuthority> authorities = codes.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new LoginUser(user, authorities);
    }
}
