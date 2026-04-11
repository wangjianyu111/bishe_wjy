package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quality_warning")
public class QualityWarning {
    @TableId(type = IdType.AUTO)
    private Long warnId;

    private Long selectionId;

    private Long teacherId;

    private Long studentId;

    /** 预警级别: 1提醒 2警告 3严重 */
    private Integer warnLevel;

    /** 预警类型: NO_GUIDANCE长期未指导 DOC_DELAY文档提交滞后 PROGRESS_DELAY进度滞后 LOW_FREQUENCY指导频率不足 OTHER */
    private String warnType;

    /** 预警原因描述 */
    private String reason;

    /** 处理状态: OPEN已开启 CLOSED已关闭 */
    private String status;

    private Long handlerId;

    private String handleComment;

    private LocalDateTime handleTime;

    private String academicYear;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
