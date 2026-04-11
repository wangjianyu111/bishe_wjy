package com.gdplatform.dto;

import lombok.Data;

@Data
public class ArchiveSubmitReq {
    private Long selectionId;
    private Long fileId;
    private String stageName;
    private String remark;
}
