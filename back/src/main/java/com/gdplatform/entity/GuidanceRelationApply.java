package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("guidance_relation_apply")
public class GuidanceRelationApply {

    @TableId(type = IdType.AUTO)
    private Long applyId;

    private Long fromUserId;

    private String fromUserName;

    private String fromUserType;

    private Long toUserId;

    private String toUserName;

    private String toUserType;

    private Long campusId;

    private String campusName;

    private String academicYear;

    private String message;

    private String status;

    private Long handleBy;

    private String handleByName;

    private LocalDateTime handleTime;

    private String handleComment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
