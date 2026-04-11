package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_monitor_alert")
public class SysMonitorAlert {

    @TableId(type = IdType.AUTO)
    private Long alertId;

    /** 告警级别：INFO WARNING ERROR CRITICAL */
    private String alertLevel;

    /** 告警类型 */
    private String alertType;

    /** 告警标题 */
    private String alertTitle;

    /** 告警内容 */
    private String alertContent;

    /** 状态：ACTIVE RESOLVED IGNORED */
    private String status;

    /** 触发时间 */
    private LocalDateTime triggerTime;

    /** 解决时间 */
    private LocalDateTime resolveTime;

    /** 解决人 */
    private Long resolvedBy;

    /** 解决人姓名 */
    private String resolvedByName;

    /** 解决备注 */
    private String resolveRemark;

    private LocalDateTime createTime;
}
