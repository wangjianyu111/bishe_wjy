package com.gdplatform.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DefenseSessionReq {

    private Long sessionId;

    private String sessionName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate defenseDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String location;

    private String defenseForm;

    private String academicYear;

    private Long fileId;

    private String remark;
}
