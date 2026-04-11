package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_school")
public class SysSchool {

    @TableId(type = IdType.AUTO)
    private Long schoolId;

    private String schoolName;

    private String schoolCode;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
