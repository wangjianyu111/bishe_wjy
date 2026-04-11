package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DefenseGroupResp {

    private Long groupId;

    private String groupName;

    private Long leaderId;

    private String leaderName;

    private Long campusId;

    private String campusName;

    private String academicYear;

    private String status;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;

    private List<DefenseGroupMemberResp> teachers;

    private List<DefenseGroupStudentResp> students;

    @Data
    public static class DefenseGroupMemberResp {
        private Long memberId;
        private Long userId;
        private String userName;
        private String teacherNo;
        private String userType;
    }

    @Data
    public static class DefenseGroupStudentResp {
        private Long studentId;
        private String studentName;
        private String studentNo;
        private Long selectionId;
        private String topicName;
    }
}
