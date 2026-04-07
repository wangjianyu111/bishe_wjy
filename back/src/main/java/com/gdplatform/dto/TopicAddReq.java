package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicAddReq {
    @NotBlank(message = "课题名称不能为空")
    private String topicName;

    private Long teacherId;

    @NotBlank(message = "学年不能为空")
    private String academicYear;

    @NotNull(message = "可选人数不能为空")
    private Integer maxStudents;

    private String description;
    private String campusName;
}
