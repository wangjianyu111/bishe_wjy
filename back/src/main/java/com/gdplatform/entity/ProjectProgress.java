package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project_progress")
public class ProjectProgress {
    @TableId(type = IdType.AUTO)
    private Long progressId;
    private Long selectionId;
    private String phase;
    private String content;
    private LocalDate planDate;
    private LocalDate actualDate;
    private Integer percentDone;
    private LocalDateTime submitTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
