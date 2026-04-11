package com.gdplatform.dto;

import lombok.Data;

@Data
public class TeacherFeedbackHandleReq {
    private Long fbId;
    private String handleComment;
    private String status;
}
