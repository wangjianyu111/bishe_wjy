package com.gdplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidanceRecordResp {

    private Long guideId;

    /** 选题ID */
    private Long selectionId;

    /** 课题名称 */
    private String topicName;

    /** 教师ID */
    private Long teacherId;

    /** 教师姓名 */
    private String teacherName;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学生学号 */
    private String studentNo;

    /** 学校名称 */
    private String campusName;

    /** 学年 */
    private String academicYear;

    /** 指导时间 */
    private LocalDateTime guideTime;

    /** 指导地点 */
    private String place;

    /** 指导时长（分钟） */
    private Integer durationMinutes;

    /** 指导方式 */
    private String guidanceType;

    /** 指导方式文本 */
    private String guidanceTypeLabel;

    /** 指导内容 */
    private String content;

    /** 附件ID */
    private Long attachmentId;

    /** 附件名称 */
    private String attachmentName;

    /** 学生反馈 */
    private String studentFeedback;

    /** 学生反馈时间 */
    private LocalDateTime feedbackTime;

    /** 状态 */
    private String status;

    /** 状态文本 */
    private String statusLabel;

    /** 该学生累计指导次数 */
    private Integer totalGuidesForStudent;

    private LocalDateTime createTime;
}
