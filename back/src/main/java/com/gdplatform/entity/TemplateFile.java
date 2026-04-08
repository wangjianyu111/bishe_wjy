package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_template_file")
public class TemplateFile {
    @TableId(type = IdType.AUTO)
    private Long templateId;

    private String phase;

    private String campusName;

    private String fileName;

    private String originalName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Long uploaderId;

    private String uploaderName;

    private LocalDateTime uploadTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}