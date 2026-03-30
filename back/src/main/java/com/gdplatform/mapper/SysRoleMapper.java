package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.RoleResp;
import com.gdplatform.entity.SysRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
            SELECT r.* FROM sys_role r
            INNER JOIN sys_user_role ur ON r.role_id = ur.role_id
            WHERE ur.user_id = #{userId} AND r.is_deleted = 0
            """)
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND is_deleted = 0 LIMIT 1")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<RoleResp> selectRolePage(@Param("keyword") String keyword, @Param("offset") long offset, @Param("limit") long limit);

    long countRolePage(@Param("keyword") String keyword);

    List<Long> selectPermIdsByRoleId(@Param("roleId") Long roleId);

    void deleteRolePermissionByRoleId(@Param("roleId") Long roleId);

    void batchInsertRolePermission(@Param("roleId") Long roleId, @Param("permIds") List<Long> permIds);
}
