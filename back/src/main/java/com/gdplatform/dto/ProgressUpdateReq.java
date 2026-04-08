package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressUpdateReq {
    private Long progressId;
    private String phase;
    private String content;
    private LocalDate planDate;
    private LocalDate actualDate;
    private Integer percentDone;
}
