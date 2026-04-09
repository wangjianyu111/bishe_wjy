package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("proposal_report")
public class ProjectProposal {
    @TableId(type = IdType.AUTO)
    private Long proposalId;
    private Long selectionId;
    private String reportContent;
    private Long fileId;
    private String status;
    private Long inspectorId;
    private String inspectComment;
    private LocalDateTime inspectTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
