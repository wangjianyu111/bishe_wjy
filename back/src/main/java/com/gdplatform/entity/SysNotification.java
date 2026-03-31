package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class SysNotification {
    @TableId(type = IdType.AUTO)
    private Long noticeId;
    private String groupId;
    private String title;
    private String content;
    private Integer noticeType;
    private Long senderId;
    private Long receiverId;
    private Integer isRead;
    private String bizType;
    private Long bizId;
    private LocalDateTime createTime;
}
