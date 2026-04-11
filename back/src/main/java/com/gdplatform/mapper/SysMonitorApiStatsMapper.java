package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.MonitorApiStatsResp;
import com.gdplatform.entity.SysMonitorApiStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysMonitorApiStatsMapper extends BaseMapper<SysMonitorApiStats> {

    Page<MonitorApiStatsResp> selectApiStatsPage(Page<MonitorApiStatsResp> page, @Param("req") com.gdplatform.dto.MonitorReq req);
}
