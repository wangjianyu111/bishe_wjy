package com.gdplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DefenseGroupCard {

    private Long groupId;

    private String groupName;

    private Long leaderId;

    private String leaderName;

    private String campusName;

    private String academicYear;

    private String status;

    private Boolean isCreator;

    private Integer teacherCount;

    private Integer studentCount;

    private List<DefenseGroupMemberItem> teachers;

    private List<DefenseGroupStudentItem> students;

    private LocalDateTime createTime;

    @Data
    public static class DefenseGroupMemberItem {
        private Long userId;
        private String userName;
        private String teacherNo;
    }

    @Data
    public static class DefenseGroupStudentItem {
        private Long studentId;
        private String studentName;
        private String studentNo;
        private String topicName;
    }
}
