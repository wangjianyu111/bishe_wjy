package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemplateResp {
    private Long templateId;
    private String phase;
    private String campusName;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime uploadTime;
}
