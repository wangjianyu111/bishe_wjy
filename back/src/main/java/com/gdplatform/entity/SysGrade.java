package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_grade")
public class SysGrade {

    @TableId(type = IdType.AUTO)
    private Long gradeId;

    private String gradeName;

    private Long schoolId;

    private Integer gradeYear;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
