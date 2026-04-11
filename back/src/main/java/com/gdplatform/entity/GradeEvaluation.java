package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩评审 - 每位教师对每个学生的单次评分记录
 * 一个学生可被多位教师评分，每位教师有独立的 regular/thesis/defense 分
 * 最终汇总分由 grade_summary 表按所有教师的平均分计算
 */
@Data
@TableName("grade_evaluation")
public class GradeEvaluation {

    @TableId(type = IdType.AUTO)
    private Long gradeId;

    /** 选题ID */
    private Long selectionId;

    /** 学生ID（冗余，便于查询） */
    private Long studentId;

    /** 教师ID（评分教师） */
    private Long evaluatorId;

    /** 指导教师姓名（冗余展示） */
    private String evaluatorName;

    /** 平时成绩（0-100） */
    private BigDecimal regularScore;

    /** 论文成绩（0-100） */
    private BigDecimal thesisScore;

    /** 答辩成绩（0-100） */
    private BigDecimal defenseScore;

    /** 本次评分总分（regular*权重 + thesis*权重 + defense*权重） */
    private BigDecimal totalScore;

    /** 评语 */
    private String comment;

    /** 是否已提交（SUBMITTED草稿 SUBMITTED -> SUBMITTED） */
    private String status;

    /** 是否已锁定（0未锁定 1已锁定，锁定后不可修改） */
    private Integer isLocked;

    /** 锁定时间 */
    private LocalDateTime lockTime;

    /** 锁定人ID */
    private Long lockedBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
