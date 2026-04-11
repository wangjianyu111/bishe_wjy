package com.gdplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningResp {
    private Long warnId;
    private Long selectionId;
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String campusName;
    private String topicName;
    private Integer warnLevel;
    private String warnLevelName;
    private String warnType;
    private String warnTypeName;
    private String reason;
    private String status;
    private String statusName;
    private Long handlerId;
    private String handlerName;
    private String handleComment;
    private String handleTime;
    private String academicYear;
    private String createTime;
}
