package com.gdplatform.dto;

import lombok.Data;

@Data
public class TeacherResp {
    private Long userId;
    private String realName;
    private String teacherNo;
    private Long campusId;
    private String campusName;
}
