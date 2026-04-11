package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("campus_threshold")
public class CampusThreshold {

    @TableId(type = IdType.AUTO)
    private Long thresholdId;

    /** 学校名称 */
    private String campusName;

    /** 学年 */
    private String academicYear;

    /** 优秀分数线（百分制，成绩大于此分数才算优秀） */
    private BigDecimal excellentScore;

    /** 管理员备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
