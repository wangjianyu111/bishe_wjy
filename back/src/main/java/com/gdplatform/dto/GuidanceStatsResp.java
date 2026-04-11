package com.gdplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuidanceStatsResp {
    /** 累计指导次数 */
    private Integer totalCount;
    /** 本学年指导次数 */
    private Integer yearlyCount;
    /** 本学期指导次数 */
    private Integer monthlyCount;
    /** 最后一次指导时间 */
    private String lastGuideTime;
}
