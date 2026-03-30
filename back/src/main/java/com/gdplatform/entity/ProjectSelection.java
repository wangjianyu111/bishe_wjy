package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_selection")
public class ProjectSelection {
    @TableId(type = IdType.AUTO)
    private Long selectionId;
    private Long studentId;
    private Long topicId;
    private String academicYear;
    private String status;
    private String applyReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
