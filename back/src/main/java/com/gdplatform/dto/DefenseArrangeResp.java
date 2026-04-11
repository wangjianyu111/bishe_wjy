package com.gdplatform.dto;

import lombok.Data;

@Data
public class DefenseArrangeResp {

    private Long arrangeId;

    private Long sessionId;

    private String sessionName;

    private String defenseDate;

    private String startTime;

    private String endTime;

    private String location;

    private String defenseForm;

    private String academicYear;

    private Long fileId;

    private String fileName;

    private String remark;

    private Long selectionId;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private String topicName;

    private String customTopicName;

    private Boolean isCustomTopic;

    private Long teacherId;

    private String teacherName;

    private String createTime;
}
