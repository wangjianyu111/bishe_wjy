package com.gdplatform.dto;

import lombok.Data;

@Data
public class RecentTopicRow {

    private Long topicId;

    private String topicName;

    private String academicYear;

    private String status;

    private String teacherName;
}
