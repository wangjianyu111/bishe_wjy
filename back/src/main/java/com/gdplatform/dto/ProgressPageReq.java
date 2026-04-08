package com.gdplatform.dto;

import lombok.Data;

@Data
public class ProgressPageReq {
    private String campusName;
    private Long teacherId;
    private Long studentId;
    private Long selectionId;
    private String academicYear;
    private String status;
    private String keyword;
}
