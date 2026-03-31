package com.gdplatform.dto;

import lombok.Data;

@Data
public class ReceiverStatusResp {
    private Long receiverId;
    private String receiverName;
    private String receiverUserName;
    private Integer isRead;
    private String noticeTime;
}
