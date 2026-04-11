package com.gdplatform.dto;

import lombok.Data;

@Data
public class VersionResp {
    private Long versionId;
    private Long selectionId;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String stageName;
    private Integer versionNo;
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String remark;
    private String status;
    private String statusLabel;
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private String reviewTime;
    private Long topicId;
    private String topicName;
    private String customTopicName;
    private Boolean isCustomTopic;
    private Long teacherId;
    private String teacherName;
    private String teacherNo;
    private String campusName;
    private String academicYear;
    private String createTime;
    private String updateTime;
}
