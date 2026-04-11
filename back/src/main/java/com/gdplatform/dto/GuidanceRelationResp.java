package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuidanceRelationResp {

    private Long relationId;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private Long teacherId;

    private String teacherName;

    private String teacherNo;

    private Long campusId;

    private String campusName;

    private String academicYear;

    private String status;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
