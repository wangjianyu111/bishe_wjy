package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_version")
public class DocVersion {
    @TableId(type = IdType.AUTO)
    private Long versionId;

    private Long selectionId;
    private Long studentId;

    private String stageName;

    private Integer versionNo;

    private Long fileId;

    private String remark;

    private String status;

    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
