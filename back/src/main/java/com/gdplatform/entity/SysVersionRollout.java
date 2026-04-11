package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_version_rollout")
public class SysVersionRollout {

    @TableId(type = IdType.AUTO)
    private Long rolloutId;

    /** 版本ID */
    private Long versionId;

    /** 操作类型：GRAY 灰度发布 / ROLLBACK 回滚 / FULL 全量发布 */
    private String rolloutType;

    /** 发布百分比 */
    private Integer rolloutPercent;

    /** 状态：PENDING 待执行 / IN_PROGRESS 执行中 / COMPLETED 已完成 / FAILED 失败 */
    private String rolloutStatus;

    /** 执行时间 */
    private LocalDateTime rolloutTime;

    /** 执行人ID */
    private Long rolloutBy;

    /** 执行人姓名 */
    private String rolloutByName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
