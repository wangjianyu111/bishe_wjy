package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.entity.SysUser;
import com.gdplatform.service.ProjectSelectionService;
import com.gdplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/selections")
@RequiredArgsConstructor
public class ProjectSelectionController {

    private final ProjectSelectionService selectionService;

    // ==================== 学生端 ====================

    /**
     * 获取所有校区列表（带题目库标记）
     */
    @GetMapping("/campuses")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<List<CampusResp>> listCampuses() {
        return R.ok(selectionService.listCampuses());
    }

    /**
     * 获取某校区的指导教师列表（带工号）
     */
    @GetMapping("/teachers")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<List<TeacherResp>> listTeachers(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String campusName) {
        if (campusName != null && !campusName.isBlank()) {
            return R.ok(selectionService.listTeachersByCampusName(campusName));
        }
        return R.ok(selectionService.listTeachersByCampus(campusId));
    }

    /**
     * 获取题目库课题列表（按学校+教师筛选）
     */
    @GetMapping("/topic-bank")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<List<TopicBankItemResp>> listTopicBank(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String academicYear) {
        return R.ok(selectionService.listTopicBankByCampus(campusId, teacherId, academicYear));
    }

    /**
     * 学生提交选题申请
     */
    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<Void> apply(@Valid @RequestBody SelectionApplyReq req) {
        selectionService.apply(req);
        return R.ok();
    }

    /**
     * 学生查看自己的选题申请记录
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<SelectionResp> getMySelection() {
        return R.ok(selectionService.getMySelection());
    }

    /**
     * 学生撤回申请
     */
    @PutMapping("/withdraw/{selectionId}")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<Void> withdraw(@PathVariable Long selectionId) {
        selectionService.withdraw(selectionId);
        return R.ok();
    }

    // ==================== 管理端 ====================

    /**
     * 分页查询选题申请列表（管理员/教师）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('project:selection:list')")
    public R<Page<SelectionResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        SelectionPageReq req = new SelectionPageReq();
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setStudentId(studentId);
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(selectionService.pageSelections(req, current, size));
    }

    /**
     * 审批通过
     */
    @PutMapping("/approve")
    @PreAuthorize("hasAuthority('project:approval:pass')")
    public R<Void> approve(@Valid @RequestBody SelectionActionReq req) {
        selectionService.approve(req);
        return R.ok();
    }

    /**
     * 审批驳回
     */
    @PutMapping("/reject")
    @PreAuthorize("hasAuthority('project:approval:reject')")
    public R<Void> reject(@Valid @RequestBody SelectionActionReq req) {
        selectionService.reject(req);
        return R.ok();
    }
}
