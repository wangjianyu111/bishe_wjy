package com.gdplatform.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 可认定成绩选项（用于下拉选择）
 */
@Data
public class GradeOptionResp {
    private Long summaryId;
    private String studentName;
    private String studentNo;
    private String campusName;
    private String academicYear;
    private BigDecimal gradeTotalScore;
    private String gradeFinalGradeLevel;
    private BigDecimal campusThreshold;
}
