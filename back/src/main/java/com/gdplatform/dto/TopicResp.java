package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TopicResp {
    private Long topicId;
    private String topicName;
    private String academicYear;
    private Integer maxStudents;
    private Integer currentCount;
    private String status;
    private String description;
    private Long campusId;
    private String campusName;
    private LocalDateTime createTime;
}
