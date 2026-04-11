package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("defense_group")
public class DefenseGroup {

    @TableId(type = IdType.AUTO)
    private Long groupId;

    private String groupName;

    private Long leaderId;

    private String leaderName;

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
