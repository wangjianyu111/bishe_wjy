package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("defense_group_member")
public class DefenseGroupMember {

    @TableId(type = IdType.AUTO)
    private Long memberId;

    private Long groupId;

    private Long userId;

    private String userName;

    private String userType;

    private Long campusId;

    private String campusName;

    private Long createBy;

    private LocalDateTime createTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
