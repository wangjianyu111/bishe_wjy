package com.gdplatform.dto;

import lombok.Data;

@Data
public class SelectionPageReq {
    private String campusName;
    private Long teacherId;
    private Long studentId;
    private String academicYear;
    private String status;
    private String keyword;
}
