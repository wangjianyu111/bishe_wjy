package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MidTermSubmitReq {
    private Long selectionId;

    private String reportContent;

    private Long fileId;
}
