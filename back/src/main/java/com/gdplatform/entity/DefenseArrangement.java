package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("defense_arrangement")
public class DefenseArrangement {

    @TableId(type = IdType.AUTO)
    private Long arrangeId;

    private Long sessionId;

    private Long selectionId;

    private Long studentId;

    private Long teacherId;

    private Integer sortOrder;

    private String status;

    private LocalDateTime createTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
