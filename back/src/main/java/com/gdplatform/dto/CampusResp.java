package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampusResp {
    private Long campusId;
    private String campusName;
    private String campusCode;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
