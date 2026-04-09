package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("thesis_document")
public class ThesisDocument {
    @TableId(type = IdType.AUTO)
    private Long thesisId;
    private Long selectionId;
    private Long studentId;
    private String reportContent;
    private Long fileId;
    private String status;
    private Long inspectorId;
    private String inspectComment;
    private LocalDateTime inspectTime;
    private Integer versionNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
