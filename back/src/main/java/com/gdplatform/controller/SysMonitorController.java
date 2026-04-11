package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.SysMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/monitor")
@RequiredArgsConstructor
public class SysMonitorController {

    private final SysMonitorService monitorService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('sysops:monitor')")
    public R<MonitorDashboardResp> getDashboard() {
        return R.ok(monitorService.getDashboard());
    }

    @GetMapping("/metrics/page")
    @PreAuthorize("hasAuthority('sysops:monitor')")
    public R<Page<MonitorMetricsResp>> pageMetrics(MonitorReq req) {
        return R.ok(monitorService.pageMetrics(req));
    }

    @GetMapping("/alerts/page")
    @PreAuthorize("hasAuthority('sysops:monitor')")
    public R<Page<MonitorAlertResp>> pageAlerts(MonitorReq req) {
        return R.ok(monitorService.pageAlerts(req));
    }

    @GetMapping("/api-stats/page")
    @PreAuthorize("hasAuthority('sysops:monitor')")
    public R<Page<MonitorApiStatsResp>> pageApiStats(MonitorReq req) {
        return R.ok(monitorService.pageApiStats(req));
    }

    @PostMapping("/alerts/resolve")
    @PreAuthorize("hasAuthority('sysops:monitor')")
    public R<Void> resolveAlert(@RequestBody MonitorAlertResolveReq req) {
        monitorService.resolveAlert(req);
        return R.ok();
    }

    @DeleteMapping("/history")
    @PreAuthorize("hasAuthority('sysops:monitor')")
    public R<Void> clearHistory(@RequestParam(defaultValue = "30") int days) {
        monitorService.clearHistory(days);
        return R.ok();
    }
}
