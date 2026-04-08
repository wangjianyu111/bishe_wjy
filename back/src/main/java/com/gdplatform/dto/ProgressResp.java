package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProgressResp {
    private Long progressId;
    private Long selectionId;
    private String phase;
    private String content;
    private LocalDate planDate;
    private LocalDate actualDate;
    private Integer percentDone;
    private LocalDateTime submitTime;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long topicId;
    private String topicName;
    private String customTopicName;
    private Boolean isCustomTopic;
    private Long teacherId;
    private String teacherName;
    private String teacherNo;
    private String campusName;
    private String academicYear;
    private String selectionStatus;
    private LocalDateTime createTime;
}
