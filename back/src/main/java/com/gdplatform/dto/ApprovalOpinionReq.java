package com.gdplatform.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ApprovalOpinionReq {

    private Long opinionId;

    /** 关联选题ID */
    private Long selectionId;

    /** 关联文档ID（可选） */
    private Long docId;

    /** 文档类型 */
    private String docType;

    /** 评审轮次 */
    private Integer roundNo;

    /** 评分 */
    private Integer score;

    /** 文字批注 */
    private String comment;

    /** 附件ID */
    private Long fileId;

    /** 状态 */
    private String status;
}
