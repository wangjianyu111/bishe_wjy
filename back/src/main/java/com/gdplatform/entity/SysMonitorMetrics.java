package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_monitor_metrics")
public class SysMonitorMetrics {

    @TableId(type = IdType.AUTO)
    private Long metricId;

    /** 指标类型：CPU MEMORY DISK NETWORK DB_CONNECTION API_RESPONSE */
    private String metricType;

    /** 指标名称 */
    private String metricName;

    /** 指标值 */
    private BigDecimal metricValue;

    /** 单位 */
    private String metricUnit;

    /** 状态：NORMAL WARNING CRITICAL */
    private String status;

    /** 阈值 */
    private BigDecimal threshold;

    /** 记录时间 */
    private LocalDateTime recordTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
}
