package com.gdplatform.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class NotificationAddReq {
    @NotEmpty(message = "通知标题不能为空")
    private String title;

    @NotEmpty(message = "通知内容不能为空")
    private String content;

    @NotNull(message = "通知类型不能为空")
    private Integer noticeType;

    private List<Long> receiverIds;

    private String bizType;

    private Long bizId;
}
