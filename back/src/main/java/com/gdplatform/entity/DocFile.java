package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_file")
public class DocFile {
    @TableId(type = IdType.AUTO)
    private Long fileId;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Long uploaderId;
    private String bizType;
    private Long bizId;
    private Long selectionId;
    private Integer versionNo;
    private LocalDateTime createTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
