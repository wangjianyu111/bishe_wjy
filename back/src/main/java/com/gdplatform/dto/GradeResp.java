package com.gdplatform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩评审响应（含明细）
 */
@Data
public class GradeResp {

    // ==================== 汇总成绩 ====================
    private Long summaryId;
    private Long selectionId;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String topicName;
    private String customTopicName;
    private Integer isCustomTopic;
    private Long teacherId;
    private String teacherName;
    private String campusName;
    private String academicYear;

    /** 参与评分的教师人数 */
    private Integer evaluatorCount;

    /** 各维度平均分 */
    private BigDecimal avgRegularScore;
    private BigDecimal avgThesisScore;
    private BigDecimal avgDefenseScore;

    /** 系统计算总分（权重：平时30% + 论文40% + 答辩30%） */
    private BigDecimal totalScore;

    /** 系统计算等级 */
    private String gradeLevel;

    /** 管理员调整后总分（null表示未调整） */
    private BigDecimal adjustedTotalScore;

    /** 最终确认总分（优先调整值，否则用系统计算值） */
    private BigDecimal finalScore;

    /** 管理员调整后等级 */
    private String adjustedGradeLevel;

    /** 最终确认等级 */
    private String finalGradeLevel;

    /** 是否有管理员调整 */
    private Boolean isAdjusted;

    /** 是否已锁定 */
    private Boolean isLocked;

    /** 管理员备注 */
    private String remark;

    private LocalDateTime lockTime;
    private String lockedByName;
    private LocalDateTime createTime;

    // ==================== 教师评分明细列表 ====================
    /** 该学生的所有教师评分记录 */
    private List<GradeDetailItem> detailList;

    // ==================== 单条明细 ====================
    @Data
    public static class GradeDetailItem {
        private Long gradeId;
        private Long evaluatorId;
        private String evaluatorName;
        private BigDecimal regularScore;
        private BigDecimal thesisScore;
        private BigDecimal defenseScore;
        private BigDecimal totalScore;
        private String comment;
        private String status;
        private Boolean isLocked;
        private LocalDateTime createTime;
    }
}
