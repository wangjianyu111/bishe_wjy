package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysGradeResp {
    private Long gradeId;
    private String gradeName;
    private Long schoolId;
    private String campusName;
    private Integer gradeYear;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
}
