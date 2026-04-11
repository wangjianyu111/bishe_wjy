package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import com.gdplatform.entity.SysMonitorAlert;
import com.gdplatform.entity.SysMonitorApiStats;
import com.gdplatform.entity.SysMonitorMetrics;
import com.gdplatform.mapper.SysMonitorAlertMapper;
import com.gdplatform.mapper.SysMonitorApiStatsMapper;
import com.gdplatform.mapper.SysMonitorMetricsMapper;
import com.gdplatform.service.SysMonitorService;
import com.gdplatform.util.SecurityUtils;
import com.zaxxer.hikari.metrics.PoolStats;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMonitorServiceImpl implements SysMonitorService {

    private final SysMonitorMetricsMapper metricsMapper;
    private final SysMonitorAlertMapper alertMapper;
    private final SysMonitorApiStatsMapper apiStatsMapper;
    private final HikariDataSource hikariDataSource;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public MonitorDashboardResp getDashboard() {
        MonitorDashboardResp resp = new MonitorDashboardResp();

        // CPU
        double cpuUsage = getCpuUsage();
        resp.setCpuUsage(BigDecimal.valueOf(cpuUsage).setScale(2, RoundingMode.HALF_UP));
        saveMetrics("CPU", "CPU 使用率", cpuUsage, "%", 80.0);

        // Memory
        double memUsage = getMemoryUsage();
        resp.setMemoryUsage(BigDecimal.valueOf(memUsage).setScale(2, RoundingMode.HALF_UP));
        saveMetrics("MEMORY", "内存使用率", memUsage, "%", 85.0);

        // Disk
        double diskUsage = getDiskUsage();
        resp.setDiskUsage(BigDecimal.valueOf(diskUsage).setScale(2, RoundingMode.HALF_UP));
        saveMetrics("DISK", "磁盘使用率", diskUsage, "%", 90.0);

        // DB
        int dbConn = getDbConnectionCount();
        resp.setDbConnectionCount(dbConn);
        resp.setDbMaxConnections(getDbMaxConnections());
        saveMetrics("DB_CONNECTION", "数据库连接数", dbConn, "个", 80.0);

        // Online users (mock for now)
        resp.setOnlineUsers(1L);

        // API stats
        QueryWrapper<SysMonitorApiStats> apiQw = new QueryWrapper<>();
        apiQw.orderByDesc("stat_time").last("LIMIT 1");
        SysMonitorApiStats todayStats = apiStatsMapper.selectOne(apiQw);
        if (todayStats != null) {
            resp.setTodayApiRequests(todayStats.getTotalRequests());
            resp.setAvgResponseTime(todayStats.getAvgResponseTime());
        } else {
            resp.setTodayApiRequests(0L);
            resp.setAvgResponseTime(BigDecimal.ZERO);
        }

        // Active alerts
        QueryWrapper<SysMonitorAlert> alertQw = new QueryWrapper<>();
        alertQw.eq("status", "ACTIVE");
        long activeCount = alertMapper.selectCount(alertQw);
        resp.setActiveAlertCount(activeCount);

        List<SysMonitorAlert> activeAlertList = alertMapper.selectList(
                alertQw.orderByDesc("trigger_time").last("LIMIT 5"));
        resp.setActiveAlerts(activeAlertList.stream().map(this::convertAlert).toList());

        // Recent metrics
        List<SysMonitorMetrics> recent = metricsMapper.selectList(
                new QueryWrapper<SysMonitorMetrics>()
                        .in("metric_type", "CPU", "MEMORY", "DISK", "DB_CONNECTION")
                        .orderByDesc("record_time")
                        .last("LIMIT 10"));
        resp.setRecentMetrics(recent.stream().map(this::convertMetrics).toList());

        return resp;
    }

    @Override
    public Page<MonitorMetricsResp> pageMetrics(MonitorReq req) {
        Page<MonitorMetricsResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<MonitorMetricsResp> result = metricsMapper.selectMetricsPage(page, req);
        for (MonitorMetricsResp r : result.getRecords()) {
            enrichMetricLabels(r);
        }
        return result;
    }

    @Override
    public Page<MonitorAlertResp> pageAlerts(MonitorReq req) {
        Page<MonitorAlertResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<MonitorAlertResp> result = alertMapper.selectAlertsPage(page, req);
        for (MonitorAlertResp r : result.getRecords()) {
            enrichAlertLabels(r);
        }
        return result;
    }

    @Override
    public Page<MonitorApiStatsResp> pageApiStats(MonitorReq req) {
        Page<MonitorApiStatsResp> page = new Page<>(req.getCurrent(), req.getSize());
        return apiStatsMapper.selectApiStatsPage(page, req);
    }

    @Override
    public void resolveAlert(MonitorAlertResolveReq req) {
        SysMonitorAlert alert = alertMapper.selectById(req.getAlertId());
        if (alert == null) {
            throw new RuntimeException("告警记录不存在");
        }
        if (!"ACTIVE".equals(alert.getStatus())) {
            throw new RuntimeException("该告警已处理");
        }
        alert.setStatus("RESOLVED");
        alert.setResolveTime(LocalDateTime.now());
        var user = SecurityUtils.currentUser();
        alert.setResolvedBy(user.getUserId());
        alert.setResolvedByName(user.getRealName());
        alert.setResolveRemark(req.getResolveRemark());
        alertMapper.updateById(alert);
    }

    @Override
    public void clearHistory(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        metricsMapper.delete(new QueryWrapper<SysMonitorMetrics>().lt("create_time", cutoff));
        apiStatsMapper.delete(new QueryWrapper<SysMonitorApiStats>().lt("create_time", cutoff));
    }

    // ==================== 内部方法 ====================

    private double getCpuUsage() {
        try {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
                return sunOs.getCpuLoad() * 100;
            }
        } catch (Exception e) {
            log.warn("获取CPU使用率失败: {}", e.getMessage());
        }
        return 0.0;
    }

    private double getMemoryUsage() {
        try {
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            long used = mem.getHeapMemoryUsage().getUsed();
            long max = mem.getHeapMemoryUsage().getMax();
            if (max <= 0) max = mem.getHeapMemoryUsage().getCommitted();
            return (double) used / max * 100;
        } catch (Exception e) {
            log.warn("获取内存使用率失败: {}", e.getMessage());
        }
        return 0.0;
    }

    private double getDiskUsage() {
        try {
            File[] roots = File.listRoots();
            long total = 0, free = 0;
            for (File root : roots) {
                total += root.getTotalSpace();
                free += root.getUsableSpace();
            }
            return total > 0 ? (double) (total - free) / total * 100 : 0.0;
        } catch (Exception e) {
            log.warn("获取磁盘使用率失败: {}", e.getMessage());
        }
        return 0.0;
    }

    private int getDbConnectionCount() {
        try {
            HikariPoolMXBean pool = hikariDataSource.getHikariPoolMXBean();
            return pool.getActiveConnections();
        } catch (Exception e) {
            log.warn("获取数据库连接数失败: {}", e.getMessage());
        }
        return 0;
    }

    private int getDbMaxConnections() {
        try {
            return hikariDataSource.getMaximumPoolSize();
        } catch (Exception e) {
            log.warn("获取数据库最大连接数失败: {}", e.getMessage());
        }
        return 0;
    }

    private void saveMetrics(String type, String name, double value, String unit, double threshold) {
        try {
            String status = value >= threshold ? "CRITICAL" : value >= threshold * 0.7 ? "WARNING" : "NORMAL";

            SysMonitorMetrics m = new SysMonitorMetrics();
            m.setMetricType(type);
            m.setMetricName(name);
            m.setMetricValue(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP));
            m.setMetricUnit(unit);
            m.setStatus(status);
            m.setThreshold(BigDecimal.valueOf(threshold));
            m.setRecordTime(LocalDateTime.now());
            m.setCreateTime(LocalDateTime.now());
            metricsMapper.insert(m);

            if ("CRITICAL".equals(status) || "WARNING".equals(status)) {
                createAlert(type, status, name, value, unit);
            }
        } catch (Exception e) {
            log.error("保存监控指标失败: {}", e.getMessage());
        }
    }

    private void createAlert(String type, String level, String name, double value, String unit) {
        QueryWrapper<SysMonitorAlert> qw = new QueryWrapper<>();
        qw.eq("alert_type", type).eq("status", "ACTIVE");
        long count = alertMapper.selectCount(qw);
        if (count > 0) return;

        SysMonitorAlert alert = new SysMonitorAlert();
        alert.setAlertLevel(level);
        alert.setAlertType(type);
        alert.setAlertTitle(name + "告警");
        alert.setAlertContent(String.format("%s 超过阈值，当前值：%.2f%s", name, value, unit));
        alert.setStatus("ACTIVE");
        alert.setTriggerTime(LocalDateTime.now());
        alert.setCreateTime(LocalDateTime.now());
        alertMapper.insert(alert);
    }

    private MonitorMetricsResp convertMetrics(SysMonitorMetrics m) {
        MonitorMetricsResp r = new MonitorMetricsResp();
        r.setMetricId(m.getMetricId());
        r.setMetricType(m.getMetricType());
        r.setMetricName(m.getMetricName());
        r.setMetricValue(m.getMetricValue());
        r.setMetricUnit(m.getMetricUnit());
        r.setStatus(m.getStatus());
        r.setThreshold(m.getThreshold());
        r.setRecordTime(m.getRecordTime());
        r.setCreateTime(m.getCreateTime());
        enrichMetricLabels(r);
        return r;
    }

    private MonitorAlertResp convertAlert(SysMonitorAlert a) {
        MonitorAlertResp r = new MonitorAlertResp();
        r.setAlertId(a.getAlertId());
        r.setAlertLevel(a.getAlertLevel());
        r.setAlertType(a.getAlertType());
        r.setAlertTitle(a.getAlertTitle());
        r.setAlertContent(a.getAlertContent());
        r.setStatus(a.getStatus());
        r.setTriggerTime(a.getTriggerTime());
        r.setResolveTime(a.getResolveTime());
        r.setResolvedBy(a.getResolvedBy());
        r.setResolvedByName(a.getResolvedByName());
        r.setResolveRemark(a.getResolveRemark());
        r.setCreateTime(a.getCreateTime());
        enrichAlertLabels(r);
        return r;
    }

    private void enrichMetricLabels(MonitorMetricsResp r) {
        r.setMetricTypeLabel(switch (r.getMetricType()) {
            case "CPU" -> "CPU 使用率";
            case "MEMORY" -> "内存使用率";
            case "DISK" -> "磁盘使用率";
            case "NETWORK" -> "网络流量";
            case "DB_CONNECTION" -> "数据库连接";
            case "API_RESPONSE" -> "接口响应时间";
            default -> r.getMetricType();
        });
        r.setStatusLabel(switch (r.getStatus()) {
            case "NORMAL" -> "正常";
            case "WARNING" -> "警告";
            case "CRITICAL" -> "严重";
            default -> r.getStatus();
        });
    }

    private void enrichAlertLabels(MonitorAlertResp r) {
        r.setAlertLevelLabel(switch (r.getAlertLevel()) {
            case "INFO" -> "通知";
            case "WARNING" -> "警告";
            case "ERROR" -> "错误";
            case "CRITICAL" -> "严重";
            default -> r.getAlertLevel();
        });
        r.setAlertTypeLabel(switch (r.getAlertType()) {
            case "CPU" -> "CPU 告警";
            case "MEMORY" -> "内存告警";
            case "DISK" -> "磁盘告警";
            case "DB_CONNECTION" -> "数据库告警";
            case "API_RESPONSE" -> "接口响应告警";
            default -> r.getAlertType();
        });
        r.setStatusLabel(switch (r.getStatus()) {
            case "ACTIVE" -> "未处理";
            case "RESOLVED" -> "已解决";
            case "IGNORED" -> "已忽略";
            default -> r.getStatus();
        });
    }
}
