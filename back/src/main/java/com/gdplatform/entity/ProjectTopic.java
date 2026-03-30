package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_topic")
public class ProjectTopic {
    @TableId(type = IdType.AUTO)
    private Long topicId;
    private String topicName;
    private Long teacherId;
    private String academicYear;
    private Integer maxStudents;
    private Integer currentCount;
    private String status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
