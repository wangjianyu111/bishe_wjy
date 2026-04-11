package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalOpinionResp {

    private Long opinionId;

    private Long selectionId;

    private Long docId;

    private String docType;

    /** 文档类型标签 */
    private String docTypeLabel;

    private Integer roundNo;

    private Long reviewerId;

    /** 评审教师姓名 */
    private String reviewerName;

    private Integer score;

    private String comment;

    private Long fileId;

    /** 附件名称 */
    private String fileName;

    private Long fileSize;

    private String mimeType;

    private String status;

    /** 状态标签 */
    private String statusLabel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // --- 选题关联信息 ---
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String topicName;
    private String customTopicName;
    private Integer isCustomTopic;
    private Long teacherId;
    private String teacherName;
    private String campusName;
    private String academicYear;
}
