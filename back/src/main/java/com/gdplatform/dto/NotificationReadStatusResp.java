package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationReadStatusResp {
    private Long noticeId;
    private String title;
    private String content;
    private Integer noticeType;
    private Long senderId;
    private String senderName;
    private Integer totalCount;
    private Integer readCount;
    private Integer unreadCount;
    private LocalDateTime createTime;
}
