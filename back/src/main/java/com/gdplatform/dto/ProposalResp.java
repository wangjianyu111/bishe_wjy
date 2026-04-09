package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProposalResp {
    private Long proposalId;
    private Long selectionId;
    private String reportContent;
    private Long fileId;
    private String fileName;
    private String status;
    private String statusLabel;
    private Long inspectorId;
    private String inspectorName;
    private String inspectComment;
    private LocalDateTime inspectTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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
}
