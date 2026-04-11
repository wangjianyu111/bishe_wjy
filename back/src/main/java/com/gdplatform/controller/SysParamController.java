package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.SysParamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/param")
@RequiredArgsConstructor
public class SysParamController {

    private final SysParamService paramService;

    // ---------- 学校 ----------
    @GetMapping("/school/page")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Page<SchoolResp>> pageSchools(ParamReq req) {
        return R.ok(paramService.pageSchools(req));
    }

    @PostMapping("/school")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> addSchool(@Valid @RequestBody ParamAddReq req) {
        paramService.addSchool(req);
        return R.ok();
    }

    @PutMapping("/school")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> updateSchool(@Valid @RequestBody ParamUpdateReq req) {
        paramService.updateSchool(req);
        return R.ok();
    }

    @DeleteMapping("/school/{id}")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> deleteSchool(@PathVariable Long id) {
        paramService.deleteSchool(id);
        return R.ok();
    }

    // ---------- 毕业设计阶段 ----------
    @GetMapping("/design-stage/page")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Page<DesignStageResp>> pageDesignStages(ParamReq req) {
        return R.ok(paramService.pageDesignStages(req));
    }

    @PostMapping("/design-stage")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> addDesignStage(@Valid @RequestBody ParamAddReq req) {
        paramService.addDesignStage(req);
        return R.ok();
    }

    @PutMapping("/design-stage")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> updateDesignStage(@Valid @RequestBody ParamUpdateReq req) {
        paramService.updateDesignStage(req);
        return R.ok();
    }

    @DeleteMapping("/design-stage/{id}")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> deleteDesignStage(@PathVariable Long id) {
        paramService.deleteDesignStage(id);
        return R.ok();
    }

    // ---------- 年级 ----------
    @GetMapping("/grade/page")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Page<SysGradeResp>> pageGrades(ParamReq req) {
        return R.ok(paramService.pageGrades(req));
    }

    @PostMapping("/grade")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> addGrade(@Valid @RequestBody ParamAddReq req) {
        paramService.addGrade(req);
        return R.ok();
    }

    @PutMapping("/grade")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> updateGrade(@Valid @RequestBody ParamUpdateReq req) {
        paramService.updateGrade(req);
        return R.ok();
    }

    @DeleteMapping("/grade/{id}")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> deleteGrade(@PathVariable Long id) {
        paramService.deleteGrade(id);
        return R.ok();
    }

    // ---------- 时间段 ----------
    @GetMapping("/time-slot/page")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Page<TimeSlotResp>> pageTimeSlots(ParamReq req) {
        return R.ok(paramService.pageTimeSlots(req));
    }

    @PostMapping("/time-slot")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> addTimeSlot(@Valid @RequestBody TimeSlotAddReq req) {
        paramService.addTimeSlot(req);
        return R.ok();
    }

    @PutMapping("/time-slot")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> updateTimeSlot(@Valid @RequestBody TimeSlotUpdateReq req) {
        paramService.updateTimeSlot(req);
        return R.ok();
    }

    @DeleteMapping("/time-slot/{id}")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> deleteTimeSlot(@PathVariable Long id) {
        paramService.deleteTimeSlot(id);
        return R.ok();
    }

    // ---------- 批量更新 ----------
    @PostMapping("/batch-update")
    @PreAuthorize("hasAuthority('sysops:param')")
    public R<Void> batchUpdate(@RequestBody ParamBatchUpdateReq req) {
        paramService.batchUpdate(req);
        return R.ok();
    }
}
