package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.WarningHandleReq;
import com.gdplatform.dto.WarningReq;
import com.gdplatform.dto.WarningResp;
import com.gdplatform.dto.WarningStatsResp;

public interface QualityWarningService {

    void add(WarningReq req);

    Page<WarningResp> pageForAdmin(long current, long size,
            String campusName, String academicYear, String warnType,
            Integer warnLevel, String status, String keyword);

    Page<WarningResp> pageForTeacher(long current, long size,
            String academicYear, String warnType,
            Integer warnLevel, String status);

    void handle(WarningHandleReq req);

    WarningStatsResp statsForAdmin(String campusName, String academicYear);

    WarningStatsResp statsForTeacher(String academicYear);

    void delete(Long warnId);
}
