package com.gdplatform.dto;

import lombok.Data;

@Data
public class ThesisSubmitReq {
    private Long selectionId;
    private String reportContent;
    private Long fileId;
}
