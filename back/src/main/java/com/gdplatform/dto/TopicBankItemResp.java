package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TopicBankItemResp {
    private Long topicId;
    private String topicName;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String teacherNo;
    private Integer maxStudents;
    private Integer currentCount;
    private String status;
    private String academicYear;
    private LocalDateTime createTime;
}
