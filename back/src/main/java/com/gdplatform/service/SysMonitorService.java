package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

public interface SysMonitorService {

    MonitorDashboardResp getDashboard();

    Page<MonitorMetricsResp> pageMetrics(MonitorReq req);

    Page<MonitorAlertResp> pageAlerts(MonitorReq req);

    Page<MonitorApiStatsResp> pageApiStats(MonitorReq req);

    void resolveAlert(MonitorAlertResolveReq req);

    void clearHistory(int days);
}
