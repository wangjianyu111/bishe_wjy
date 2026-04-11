package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DesignStageResp {
    private Long stageId;
    private String stageName;
    private Long schoolId;
    private String campusName;
    private Integer stageOrder;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
}
