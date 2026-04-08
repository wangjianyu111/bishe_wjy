package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressAddReq {
    private Long selectionId;
    private String phase;
    private String content;
    private LocalDate planDate;
}
