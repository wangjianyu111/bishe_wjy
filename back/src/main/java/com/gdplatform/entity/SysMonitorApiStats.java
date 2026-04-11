package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_monitor_api_stats")
public class SysMonitorApiStats {

    @TableId(type = IdType.AUTO)
    private Long statId;

    /** 接口路径 */
    private String apiPath;

    /** 接口名称 */
    private String apiName;

    /** 请求方法 */
    private String requestMethod;

    /** 平均响应时间(ms) */
    private BigDecimal avgResponseTime;

    /** 最大响应时间(ms) */
    private Long maxResponseTime;

    /** 最小响应时间(ms) */
    private Long minResponseTime;

    /** 请求总数 */
    private Long totalRequests;

    /** 成功次数 */
    private Long successCount;

    /** 失败次数 */
    private Long failCount;

    /** TPS */
    private BigDecimal tps;

    /** 统计时间 */
    private LocalDateTime statTime;

    private LocalDateTime createTime;
}
