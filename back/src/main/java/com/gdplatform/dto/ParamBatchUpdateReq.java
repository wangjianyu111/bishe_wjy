package com.gdplatform.dto;

import lombok.Data;
import java.util.List;

@Data
public class ParamBatchUpdateReq {
    private String type;
    private List<ParamUpdateReq> items;
}
