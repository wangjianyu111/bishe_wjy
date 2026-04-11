package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.MonitorAlertResp;
import com.gdplatform.entity.SysMonitorAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysMonitorAlertMapper extends BaseMapper<SysMonitorAlert> {

    Page<MonitorAlertResp> selectAlertsPage(Page<MonitorAlertResp> page, @Param("req") com.gdplatform.dto.MonitorReq req);
}
