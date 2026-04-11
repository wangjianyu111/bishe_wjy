package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("excellent_achievement")
public class ExcellentAchievement {

    @TableId(type = IdType.AUTO)
    private Long excellentId;

    /** 关联的成绩汇总ID */
    private Long gradeSummaryId;

    /** 选题ID（冗余） */
    private Long selectionId;

    /** 学生ID */
    private Long studentId;

    /** 最终认定总分 */
    private BigDecimal finalScore;

    /** 最终认定等级（冗余，固定为"优秀"） */
    private String finalGradeLevel;

    /** 认定状态：PENDING待认定 APPROVED已认定 REJECTED已驳回 */
    private String status;

    /** 认定人ID */
    private Long approverId;

    /** 认定人姓名（冗余） */
    private String approverName;

    /** 认定备注 */
    private String remark;

    /** 是否已导出（0否 1是） */
    private Integer isExported;

    private LocalDateTime approveTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
