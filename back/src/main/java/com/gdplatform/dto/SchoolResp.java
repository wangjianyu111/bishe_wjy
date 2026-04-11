package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SchoolResp {
    private Long schoolId;
    private String campusName;
    private String campusCode;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
}
