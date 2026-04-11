package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_opinion")
public class ApprovalOpinion {

    @TableId(type = IdType.AUTO)
    private Long opinionId;

    /** 关联选题ID */
    private Long selectionId;

    /** 关联文档ID（可选，关联到具体文档） */
    private Long docId;

    /** 文档类型（PROPOSAL/ midterm/THESIS/ARCHIVE） */
    private String docType;

    /** 评审轮次（第1轮、第2轮...） */
    private Integer roundNo;

    /** 评审教师ID */
    private Long reviewerId;

    /** 评分（0-100，可为空） */
    private Integer score;

    /** 文字批注/意见 */
    private String comment;

    /** 附件ID（评审附件） */
    private Long fileId;

    /** 状态：SUBMITTED已提交 PASSED通过 FAILED驳回 */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
