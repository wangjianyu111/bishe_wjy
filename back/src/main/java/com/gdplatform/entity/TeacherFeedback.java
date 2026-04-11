package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_feedback")
public class TeacherFeedback {
    @TableId(type = IdType.AUTO)
    private Long fbId;

    private Long selectionId;

    private Long teacherId;

    private Long studentId;

    /** 反馈类型: TEACHING_QUALITY/STUDENT_ISSUE/SYSTEM_IMPROVE/RESOURCE_LACK/OTHER */
    private String feedbackType;

    /** 反馈内容 */
    private String content;

    /** 处理状态: PENDING/HANDLING/RESOLVED/REJECTED */
    private String status;

    private Long handlerId;

    private String handleComment;

    private LocalDateTime handleTime;

    private String academicYear;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
