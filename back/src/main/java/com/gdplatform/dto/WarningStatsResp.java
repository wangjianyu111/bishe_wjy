package com.gdplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningStatsResp {
    private int totalCount;
    private int levelOneCount;
    private int levelTwoCount;
    private int levelThreeCount;
    private int openCount;
    private int closedCount;
    private int noGuidanceCount;
    private int docDelayCount;
    private int progressDelayCount;
    private int lowFrequencyCount;
    private int otherCount;
}
