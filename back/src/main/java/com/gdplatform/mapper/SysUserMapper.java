package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.UserResp;
import com.gdplatform.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE user_name = #{userName} AND status != 0 AND is_deleted = 0 LIMIT 1")
    SysUser selectActiveUserByUsername(@Param("userName") String userName);

    List<UserResp> selectUserPage(@Param("keyword") String keyword, @Param("offset") long offset, @Param("limit") long limit);

    long countUserPage(@Param("keyword") String keyword);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM sys_user_role ur " +
            "INNER JOIN sys_role r ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{userId} AND r.role_code = 'ROLE_ADMIN' AND r.is_deleted = 0")
    boolean isAdminByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM sys_user WHERE user_name = #{userName} AND is_deleted = 0")
    boolean existsByUserName(@Param("userName") String userName);

    void deleteUserRoleByUserId(@Param("userId") Long userId);

    void batchInsertUserRole(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
