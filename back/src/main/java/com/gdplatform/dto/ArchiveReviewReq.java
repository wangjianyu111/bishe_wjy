package com.gdplatform.dto;

import lombok.Data;

@Data
public class ArchiveReviewReq {
    private Long archiveId;
    private String status;
    private String reviewComment;
}
