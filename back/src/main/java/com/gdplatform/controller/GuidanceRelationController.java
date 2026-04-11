package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.GuidanceRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guidance/relation")
@RequiredArgsConstructor
public class GuidanceRelationController {

    private final GuidanceRelationService relationService;

    // ==================== 原有列表接口 ====================

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<Page<GuidanceRelationResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(relationService.pageForAdmin(current, size, campusName, academicYear, keyword));
    }

    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<Page<GuidanceRelationResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(relationService.pageForTeacher(current, size, academicYear, keyword));
    }

    @GetMapping("/student/page")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<Page<GuidanceRelationResp>> studentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear) {
        return R.ok(relationService.pageForStudent(current, size, academicYear));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('guidance:relation:add')")
    public R<Void> add(@Valid @RequestBody GuidanceRelationReq req) {
        relationService.add(req);
        return R.ok();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('guidance:relation:edit')")
    public R<Void> update(@Valid @RequestBody GuidanceRelationReq req) {
        relationService.update(req);
        return R.ok();
    }

    @DeleteMapping("/{relationId}")
    @PreAuthorize("hasAuthority('guidance:relation:del')")
    public R<Void> delete(@PathVariable Long relationId) {
        relationService.terminateRelation(relationId);
        return R.ok();
    }

    @GetMapping("/{relationId}")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<GuidanceRelationResp> getDetail(@PathVariable Long relationId) {
        return R.ok(relationService.getDetail(relationId));
    }

    @GetMapping("/teacher/students")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<List<GuidanceRelationResp>> listByTeacher(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String academicYear) {
        return R.ok(relationService.listByTeacher(teacherId, academicYear));
    }

    // ==================== 教师仪表盘 ====================

    @GetMapping("/teacher/dashboard/students")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<Page<GuidanceRelationPageResp>> teacherStudentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(relationService.teacherStudentPage(current, size, academicYear, keyword));
    }

    @GetMapping("/teacher/dashboard/groups/created")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<List<DefenseGroupCard>> teacherCreatedGroups(
            @RequestParam(required = false) String academicYear) {
        return R.ok(relationService.teacherCreatedGroups(academicYear));
    }

    @GetMapping("/teacher/dashboard/groups/joined")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<List<DefenseGroupCard>> teacherJoinedGroups(
            @RequestParam(required = false) String academicYear) {
        return R.ok(relationService.teacherJoinedGroups(academicYear));
    }

    // ==================== 学生仪表盘 ====================

    @GetMapping("/student/dashboard/relation")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<GuidanceRelationPageResp> studentRelation(
            @RequestParam(required = false) String academicYear) {
        return R.ok(relationService.studentRelation(academicYear));
    }

    @GetMapping("/student/dashboard/groups")
    @PreAuthorize("hasAuthority('guidance:relation:view')")
    public R<List<DefenseGroupCard>> studentGroups(
            @RequestParam(required = false) String academicYear) {
        return R.ok(relationService.studentJoinedGroups(academicYear));
    }

    // ==================== 申请流程 ====================

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('guidance:relation:add')")
    public R<Void> sendApply(@Valid @RequestBody GuidanceRelationApplyReq req) {
        relationService.sendApply(req);
        return R.ok();
    }

    @GetMapping("/apply/received")
    @PreAuthorize("hasAuthority('guidance:relation:handle')")
    public R<Page<GuidanceRelationApplyResp>> myReceivedApplies(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return R.ok(relationService.myReceivedApplies(current, size));
    }

    @GetMapping("/apply/sent")
    @PreAuthorize("hasAuthority('guidance:relation:add')")
    public R<Page<GuidanceRelationApplyResp>> mySentApplies(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return R.ok(relationService.mySentApplies(current, size));
    }

    @PutMapping("/apply/handle")
    @PreAuthorize("hasAuthority('guidance:relation:handle')")
    public R<Void> handleApply(@Valid @RequestBody GuidanceRelationHandleReq req) {
        relationService.handleApply(req);
        return R.ok();
    }

    @DeleteMapping("/apply/{applyId}")
    @PreAuthorize("hasAuthority('guidance:relation:add')")
    public R<Void> cancelApply(@PathVariable Long applyId) {
        relationService.cancelApply(applyId);
        return R.ok();
    }
}
