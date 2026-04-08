package com.gdplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfile {
    private Long userId;
    private String userName;
    private String realName;
    private Integer userType;
    private String studentNo;
    private String teacherNo;
    private String campusName;
    private List<String> roles;
}
