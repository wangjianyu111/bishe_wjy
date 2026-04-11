package com.gdplatform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优秀成果响应对象
 */
@Data
public class ExcellentAchievementResp {

    // ==================== 优秀成果信息 ====================
    private Long excellentId;
    private Long gradeSummaryId;
    private Long selectionId;
    private Long studentId;

    /** 最终认定总分 */
    private BigDecimal finalScore;

    /** 最终认定等级（固定为"优秀"） */
    private String finalGradeLevel;

    /** 认定状态：PENDING待认定 APPROVED已认定 REJECTED已驳回 */
    private String status;

    private Long approverId;
    private String approverName;
    private String remark;

    /** 是否已导出 */
    private Boolean isExported;

    private LocalDateTime approveTime;
    private LocalDateTime createTime;

    // ==================== 关联的成绩信息 ====================
    private String studentName;
    private String studentNo;
    private String topicName;
    private String customTopicName;
    private Integer isCustomTopic;
    private Long teacherId;
    private String teacherName;
    private String campusName;
    private String academicYear;

    /** 原始成绩总分（来自成绩汇总） */
    private BigDecimal gradeTotalScore;

    /** 原始成绩等级 */
    private String gradeFinalGradeLevel;

    /** 平时均分 */
    private BigDecimal avgRegularScore;

    /** 论文均分 */
    private BigDecimal avgThesisScore;

    /** 答辩均分 */
    private BigDecimal avgDefenseScore;

    /** 参与评分教师数 */
    private Integer evaluatorCount;

    /** 是否有管理员调整 */
    private Boolean isAdjusted;

    /** 是否已锁定 */
    private Boolean isLocked;

    // ==================== 阈值信息 ====================
    /** 该学校/学年的优秀分数线 */
    private BigDecimal campusThreshold;
}
