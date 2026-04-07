package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TopicResp {
    private Long topicId;
    private String topicName;
    private Long teacherId;
    private String teacherName;
    private String academicYear;
    private Integer maxStudents;
    private Integer currentCount;
    private String status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
