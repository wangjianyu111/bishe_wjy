package com.gdplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherFeedbackResp {
    private Long fbId;
    private Long selectionId;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String topicName;
    private String feedbackType;
    private String feedbackTypeName;
    private String content;
    private String status;
    private String statusName;
    private Long handlerId;
    private String handlerName;
    private String handleComment;
    private String handleTime;
    private String academicYear;
    private String createTime;
}
