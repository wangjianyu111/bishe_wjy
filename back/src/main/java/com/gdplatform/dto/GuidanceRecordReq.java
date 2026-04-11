package com.gdplatform.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class GuidanceRecordReq {

    private Long guideId;

    /** 选题ID */
    private Long selectionId;

    /** 指导时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime guideTime;

    /** 指导地点 */
    private String place;

    /** 指导时长（分钟） */
    private Integer durationMinutes;

    /** 指导方式 */
    private String guidanceType;

    /** 指导内容 */
    private String content;

    /** 附件ID */
    private Long attachmentId;
}
