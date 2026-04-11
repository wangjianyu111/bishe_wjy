package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 学校优秀分数线设置请求
 */
@Data
public class CampusThresholdReq {

    /** 记录ID（更新时传，新增时不传） */
    private Long thresholdId;

    /** 学校名称 */
    @NotBlank(message = "学校名称不能为空")
    private String campusName;

    /** 学年 */
    @NotBlank(message = "学年不能为空")
    private String academicYear;

    /** 优秀分数线（百分制） */
    @NotNull(message = "优秀分数线不能为空")
    private BigDecimal excellentScore;

    /** 备注 */
    private String remark;
}
