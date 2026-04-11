package com.gdplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackStatsResp {
    private Integer totalCount;
    private Integer pendingCount;
    private Integer handlingCount;
    private Integer resolvedCount;
    private Integer rejectedCount;
    private Integer teachingQualityCount;
    private Integer studentIssueCount;
    private Integer systemImproveCount;
    private Integer resourceLackCount;
    private Integer otherCount;
}
