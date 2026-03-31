package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResp {
    private Long noticeId;
    private String groupId;
    private String title;
    private String content;
    private Integer noticeType;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Integer isRead;
    private String bizType;
    private Long bizId;
    private LocalDateTime createTime;
    /** 接收人总数（同一 groupId 下） */
    private Integer receiverCount;
    /** 接收人名称列表（逗号拼接，最多显示3个，其余显示 +N） */
    private String receiverNames;
    /** 同一 groupId 中已读人数 */
    private Integer readCount;
}
