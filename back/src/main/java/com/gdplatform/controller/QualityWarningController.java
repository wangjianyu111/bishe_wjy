package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.QualityWarningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guidance/warning")
@RequiredArgsConstructor
public class QualityWarningController {

    private final QualityWarningService warningService;

    @PostMapping
    @PreAuthorize("hasAuthority('guidance:warning:add')")
    public R<Void> add(@Valid @RequestBody WarningReq req) {
        warningService.add(req);
        return R.ok();
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('guidance:warning')")
    public R<Page<WarningResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String warnType,
            @RequestParam(required = false) Integer warnLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return R.ok(warningService.pageForAdmin(current, size, campusName, academicYear,
                warnType, warnLevel, status, keyword));
    }

    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('guidance:warning:add')")
    public R<Page<WarningResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String warnType,
            @RequestParam(required = false) Integer warnLevel,
            @RequestParam(required = false) String status) {
        return R.ok(warningService.pageForTeacher(current, size, academicYear,
                warnType, warnLevel, status));
    }

    @PutMapping("/handle")
    @PreAuthorize("hasAuthority('guidance:warning')")
    public R<Void> handle(@RequestBody WarningHandleReq req) {
        warningService.handle(req);
        return R.ok();
    }

    @DeleteMapping("/{warnId}")
    @PreAuthorize("hasAuthority('guidance:warning:del')")
    public R<Void> delete(@PathVariable Long warnId) {
        warningService.delete(warnId);
        return R.ok();
    }

    @GetMapping("/stats/admin")
    @PreAuthorize("hasAuthority('guidance:warning')")
    public R<WarningStatsResp> statsForAdmin(
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear) {
        return R.ok(warningService.statsForAdmin(campusName, academicYear));
    }

    @GetMapping("/stats/teacher")
    @PreAuthorize("hasAuthority('guidance:warning:add')")
    public R<WarningStatsResp> statsForTeacher(
            @RequestParam(required = false) String academicYear) {
        return R.ok(warningService.statsForTeacher(academicYear));
    }
}
