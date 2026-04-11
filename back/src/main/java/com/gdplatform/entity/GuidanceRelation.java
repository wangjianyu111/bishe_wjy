package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("guidance_relation")
public class GuidanceRelation {

    @TableId(type = IdType.AUTO)
    private Long relationId;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private Long teacherId;

    private String teacherName;

    private String teacherNo;

    private Long campusId;

    private String campusName;

    private String academicYear;

    private String status;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
