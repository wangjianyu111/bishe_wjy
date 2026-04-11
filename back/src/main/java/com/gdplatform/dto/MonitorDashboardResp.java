package com.gdplatform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MonitorDashboardResp {
    /** CPU 使用率 */
    private BigDecimal cpuUsage;

    /** 内存使用率 */
    private BigDecimal memoryUsage;

    /** 磁盘使用率 */
    private BigDecimal diskUsage;

    /** 数据库连接数 */
    private Integer dbConnectionCount;

    /** 数据库最大连接数 */
    private Integer dbMaxConnections;

    /** 在线用户数 */
    private Long onlineUsers;

    /** 今日 API 请求总数 */
    private Long todayApiRequests;

    /** API 平均响应时间 */
    private BigDecimal avgResponseTime;

    /** 告警数量 */
    private Long activeAlertCount;

    /** 各指标最新数据 */
    private java.util.List<MonitorMetricsResp> recentMetrics;

    /** 活跃告警 */
    private java.util.List<MonitorAlertResp> activeAlerts;
}
