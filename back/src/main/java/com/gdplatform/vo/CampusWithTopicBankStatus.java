package com.gdplatform.vo;

import lombok.Data;

@Data
public class CampusWithTopicBankStatus {
    private Long campusId;
    private String campusName;
    private String campusCode;
    private Boolean hasTopicBank;
}
