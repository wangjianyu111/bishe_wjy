package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_mid_term")
public class ProjectMidTerm {
    @TableId(type = IdType.AUTO)
    private Long midId;
    private Long selectionId;
    private String reportContent;
    private Long fileId;
    private String status;
    private Long inspectorId;
    private String inspectComment;
    private LocalDateTime inspectTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
