package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩汇总表 - 每个学生最终只有一个汇总成绩
 * total_score 由系统根据所有教师的评分自动计算（平均分）
 * grade_level 由系统根据 total_score 自动判定
 * 管理员可人工调整最终成绩，调整后 is_adjusted=1
 * 管理员可锁定，锁定后所有教师无法再提交/修改该学生的评分
 */
@Data
@TableName("grade_summary")
public class GradeSummary {

    @TableId(type = IdType.AUTO)
    private Long summaryId;

    /** 选题ID */
    private Long selectionId;

    /** 学生ID */
    private Long studentId;

    /** 参与评分的教师数量 */
    private Integer evaluatorCount;

    /** 各教师平时均分 */
    private BigDecimal avgRegularScore;

    /** 各教师论文均分 */
    private BigDecimal avgThesisScore;

    /** 各教师答辩均分 */
    private BigDecimal avgDefenseScore;

    /** 最终总分（平时*30% + 论文*40% + 答辩*30%） */
    private BigDecimal totalScore;

    /** 成绩等级：优秀/良好/中等/及格/不及格 */
    private String gradeLevel;

    /** 优秀/良好/中等/及格/不及格对应百分制下限：90/80/70/60/0 */
    private String levelStandard;

    /** 各教师评分记录数量 */
    private Integer recordCount;

    /** 管理员调整后的总分（若未调整则为null） */
    private BigDecimal adjustedTotalScore;

    /** 最终确认的总分（优先用调整值，否则用系统计算值） */
    private BigDecimal finalScore;

    /** 管理员调整后的等级 */
    private String adjustedGradeLevel;

    /** 最终确认的等级 */
    private String finalGradeLevel;

    /** 是否有管理员调整（0否 1是） */
    private Integer isAdjusted;

    /** 是否已锁定（0未锁定 1已锁定） */
    private Integer isLocked;

    /** 锁定时间 */
    private LocalDateTime lockTime;

    /** 锁定人 */
    private Long lockedBy;

    /** 管理员备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
