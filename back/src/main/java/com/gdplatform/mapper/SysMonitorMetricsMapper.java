package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.MonitorMetricsResp;
import com.gdplatform.entity.SysMonitorMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysMonitorMetricsMapper extends BaseMapper<SysMonitorMetrics> {

    Page<MonitorMetricsResp> selectMetricsPage(Page<MonitorMetricsResp> page, @Param("req") com.gdplatform.dto.MonitorReq req);
}
