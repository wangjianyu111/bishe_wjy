package com.gdplatform.dto;

import lombok.Data;

@Data
public class VersionSubmitReq {
    private Long selectionId;
    private String stageName;
    private Integer versionNo;
    private String remark;
    private Long fileId;
}
