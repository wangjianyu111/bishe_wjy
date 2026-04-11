package com.gdplatform.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理员调整成绩请求
 */
@Data
public class GradeAdjustReq {

    @NotNull(message = "汇总成绩ID不能为空")
    private Long summaryId;

    /** 调整后的总分 */
    @NotNull(message = "调整总分不能为空")
    @DecimalMin(value = "0", message = "总分不能小于0")
    @DecimalMax(value = "100", message = "总分不能大于100")
    private BigDecimal adjustedTotalScore;

    /** 调整后的等级 */
    private String adjustedGradeLevel;

    /** 备注 */
    private String remark;
}
