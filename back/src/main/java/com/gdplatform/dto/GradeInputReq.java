package com.gdplatform.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 录入成绩请求
 */
@Data
public class GradeInputReq {

    private Long gradeId;

    /** 选题ID（新增时必填） */
    @NotNull(message = "选题不能为空")
    private Long selectionId;

    /** 学生ID（新增时必填） */
    @NotNull(message = "学生不能为空")
    private Long studentId;

    /** 平时成绩（0-100） */
    @DecimalMin(value = "0", message = "平时成绩不能小于0")
    @DecimalMax(value = "100", message = "平时成绩不能大于100")
    private BigDecimal regularScore;

    /** 论文成绩（0-100） */
    @DecimalMin(value = "0", message = "论文成绩不能小于0")
    @DecimalMax(value = "100", message = "论文成绩不能大于100")
    private BigDecimal thesisScore;

    /** 答辩成绩（0-100） */
    @DecimalMin(value = "0", message = "答辩成绩不能小于0")
    @DecimalMax(value = "100", message = "答辩成绩不能大于100")
    private BigDecimal defenseScore;

    /** 评语 */
    private String comment;
}
