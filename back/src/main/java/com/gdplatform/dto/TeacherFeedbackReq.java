package com.gdplatform.dto;

import lombok.Data;

@Data
public class TeacherFeedbackReq {
    private Long selectionId;
    private String feedbackType;
    private String content;
}
