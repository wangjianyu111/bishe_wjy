package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.PermissionTreeNode;
import com.gdplatform.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    List<PermissionTreeNode> selectAllAsTree();

    List<SysPermission> selectAllList();
}
