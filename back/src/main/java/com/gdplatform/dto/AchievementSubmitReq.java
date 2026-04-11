package com.gdplatform.dto;

import lombok.Data;

@Data
public class AchievementSubmitReq {
    private Long selectionId;
    private Long fileId;
    private String remark;
}
