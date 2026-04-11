package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("defense_session")
public class DefenseSession {

    @TableId(type = IdType.AUTO)
    private Long sessionId;

    private String sessionName;

    private LocalDate defenseDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String location;

    private String defenseForm;

    private String academicYear;

    private Long fileId;

    private String remark;

    private Long teacherId;

    private String campusName;

    private LocalDateTime createTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
