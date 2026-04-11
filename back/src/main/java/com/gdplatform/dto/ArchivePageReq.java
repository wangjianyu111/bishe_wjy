package com.gdplatform.dto;

import lombok.Data;

@Data
public class ArchivePageReq {
    private Long selectionId;
    private Long studentId;
    private Long teacherId;
    private String campusName;
    private String academicYear;
    private String stageName;
    private String keyword;
}
