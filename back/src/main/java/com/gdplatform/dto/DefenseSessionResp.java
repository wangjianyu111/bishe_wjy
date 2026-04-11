package com.gdplatform.dto;

import lombok.Data;

@Data
public class DefenseSessionResp {

    private Long sessionId;

    private String sessionName;

    private String defenseDate;

    private String startTime;

    private String endTime;

    private String location;

    private String defenseForm;

    private String academicYear;

    private String campusName;

    private Long teacherId;

    private Long fileId;

    private String fileName;

    private Long fileSize;

    private String mimeType;

    private String remark;

    private String createTime;
}
