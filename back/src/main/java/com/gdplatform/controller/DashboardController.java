package com.gdplatform.controller;

import com.gdplatform.common.R;
import com.gdplatform.dto.DashboardOverviewResp;
import com.gdplatform.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public R<DashboardOverviewResp> overview() {
        return R.ok(dashboardService.overview());
    }
}
