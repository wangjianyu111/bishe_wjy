package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SelectionResp {
    private Long selectionId;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long topicId;
    private String topicName;
    private String topicDescription;
    private Long teacherId;
    private String teacherName;
    private String teacherNo;
    private String campusName;
    private String academicYear;
    private String status;
    private String statusLabel;
    private String applyReason;
    private String rejectReason;
    private Boolean isCustomTopic;
    private String customTopicName;
    private String customTopicDescription;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
