package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("guidance_record")
public class GuidanceRecord {

    @TableId(type = IdType.AUTO)
    private Long guideId;

    /** 选题ID */
    private Long selectionId;

    /** 教师ID */
    private Long teacherId;

    /** 学生ID */
    private Long studentId;

    /** 指导时间 */
    private LocalDateTime guideTime;

    /** 指导地点 */
    private String place;

    /** 指导时长（分钟） */
    private Integer durationMinutes;

    /** 指导方式：GUIDANCE指导 VISIT走访 GROUP座谈 ONLINE线上 OTHER其他 */
    private String guidanceType;

    /** 指导内容 */
    private String content;

    /** 附件ID（可选） */
    private Long attachmentId;

    /** 学生反馈 */
    private String studentFeedback;

    /** 学生反馈时间 */
    private LocalDateTime feedbackTime;

    /** 状态：DRAFT草稿 PUBLISHED已发布 */
    private String status;

    /** 学年（从 project_selection 同步） */
    private String academicYear;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
