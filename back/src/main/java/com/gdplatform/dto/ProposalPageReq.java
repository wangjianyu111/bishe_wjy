package com.gdplatform.dto;

import lombok.Data;

@Data
public class ProposalPageReq {
    private Long selectionId;
    private String campusName;
    private Long teacherId;
    private Long studentId;
    private String academicYear;
    private String status;
    private String keyword;
}
