package com.gdplatform.dto;

import lombok.Data;

@Data
public class AchievementResp {
    private Long submitId;
    private Long selectionId;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long topicId;
    private String topicName;
    private String customTopicName;
    private Boolean isCustomTopic;
    private Long teacherId;
    private String teacherName;
    private String campusName;
    private String academicYear;
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String remark;
    private String createTime;
    private String updateTime;
}
