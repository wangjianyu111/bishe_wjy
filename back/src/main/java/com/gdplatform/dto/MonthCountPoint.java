package com.gdplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthCountPoint {

    /** yyyy-MM */
    private String month;

    private Long total;
}
