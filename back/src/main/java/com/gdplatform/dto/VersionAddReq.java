package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VersionAddReq {

    @NotBlank(message = "版本号不能为空")
    private String versionCode;

    @NotBlank(message = "版本名称不能为空")
    private String versionName;

    @NotBlank(message = "应用类型不能为空")
    private String appType;

    @NotBlank(message = "发布类型不能为空")
    private String releaseType;

    private Integer rolloutPercent = 0;

    private String changelog;

    private String downloadUrl;

    private String minCompatibleVersion;

    private String featureList;

    private Integer forceUpdate = 0;
}
