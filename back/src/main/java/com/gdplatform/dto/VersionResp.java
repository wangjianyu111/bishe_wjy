package com.gdplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VersionResp {
    private Long versionId;
    private String versionCode;
    private String versionName;
    private String appType;
    private String appTypeLabel;
    private String status;
    private String statusLabel;
    private String releaseType;
    private String releaseTypeLabel;
    private Integer rolloutPercent;
    private String changelog;
    private String downloadUrl;
    private String minCompatibleVersion;
    private String featureList;
    private Long deployedBy;
    private String deployedByName;
    private LocalDateTime deployedTime;
    private Integer forceUpdate;
    private LocalDateTime createTime;
}
