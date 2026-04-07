package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SelectionApplyReq {
    private String campusName;
    private Long teacherId;
    private Long topicId;
    private String customTopicName;
    private String customTopicDescription;
    @NotBlank(message = "学年不能为空")
    private String academicYear;
    private String applyReason;
}
