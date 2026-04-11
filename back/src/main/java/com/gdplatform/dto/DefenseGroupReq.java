package com.gdplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DefenseGroupReq {

    private Long groupId;

    @NotBlank(message = "小组名称不能为空")
    private String groupName;

    @NotNull(message = "请选择答辩教师")
    private List<Long> teacherIds;

    private List<Long> studentIds;

    private String academicYear;

    private String status;
}
