package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 优秀成果认定请求
 */
@Data
public class ExcellentAchievementReq {

    /** 成绩汇总ID */
    @NotNull(message = "成绩汇总ID不能为空")
    private Long gradeSummaryId;

    /** 认定备注 */
    private String remark;
}
