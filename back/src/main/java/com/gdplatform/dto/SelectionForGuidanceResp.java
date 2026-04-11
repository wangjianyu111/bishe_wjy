package com.gdplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionForGuidanceResp {
    private Long selectionId;
    private String studentName;
    private String studentNo;
    private String topicName;
    private String customTopicName;
    private Integer isCustomTopic;
    private String academicYear;
    private String status;
    private String createTime;
}
