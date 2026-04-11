package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_design_stage")
public class SysDesignStage {

    @TableId(type = IdType.AUTO)
    private Long stageId;

    private String stageName;

    private Long schoolId;

    private Integer stageOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
