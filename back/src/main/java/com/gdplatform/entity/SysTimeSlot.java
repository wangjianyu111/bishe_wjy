package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_time_slot")
public class SysTimeSlot {

    @TableId(type = IdType.AUTO)
    private Long slotId;

    private String slotName;

    private Long schoolId;

    private String slotType;

    private String startDate;

    private String endDate;

    private Integer slotOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
