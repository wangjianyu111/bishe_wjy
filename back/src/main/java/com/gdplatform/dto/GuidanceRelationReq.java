package com.gdplatform.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GuidanceRelationReq {

    private Long relationId;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    private String academicYear;

    private String status;
}
