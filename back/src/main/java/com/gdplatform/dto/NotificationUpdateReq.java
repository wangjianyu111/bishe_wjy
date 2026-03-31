package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationUpdateReq {
    @NotNull(message = "通知ID不能为空")
    private Long noticeId;

    private String title;
    private String content;
    private Integer noticeType;
}
