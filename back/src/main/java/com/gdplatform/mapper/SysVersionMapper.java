package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.VersionResp;
import com.gdplatform.entity.SysVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysVersionMapper extends BaseMapper<SysVersion> {

    Page<VersionResp> selectVersionPage(Page<VersionResp> page, @Param("req") com.gdplatform.dto.VersionReq req);
}
