package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GuidanceRelationPageResp {

    private Long relationId;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private Long teacherId;

    private String teacherName;

    private String teacherNo;

    private String campusName;

    private String academicYear;

    private String status;

    private LocalDateTime createTime;

    private List<DefenseGroupCard> defenseGroups;
}
