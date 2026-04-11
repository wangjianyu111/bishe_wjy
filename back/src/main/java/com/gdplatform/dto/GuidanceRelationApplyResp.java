package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuidanceRelationApplyResp {

    private Long applyId;

    private Long fromUserId;

    private String fromUserName;

    private String fromUserType;

    private Long toUserId;

    private String toUserName;

    private String toUserType;

    private String campusName;

    private String academicYear;

    private String message;

    private String status;

    private String handleByName;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;
}
