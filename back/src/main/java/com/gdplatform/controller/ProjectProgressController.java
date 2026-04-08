package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.entity.SysUser;
import com.gdplatform.service.ProjectProgressService;
import com.gdplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/progress")
@RequiredArgsConstructor
public class ProjectProgressController {

    private final ProjectProgressService progressService;

    // ==================== 学生端 ====================

    /**
     * 获取自己的选题进度列表（分页）
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('project:progress:list')")
    public R<Page<ProgressResp>> getMyProgressPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear) {
        ProgressPageReq req = new ProgressPageReq();
        req.setAcademicYear(academicYear);
        return R.ok(progressService.pageProgressForStudent(req, current, size));
    }

    /**
     * 获取某个选题的所有进度记录
     */
    @GetMapping("/selection/{selectionId}")
    @PreAuthorize("hasAuthority('project:progress:list')")
    public R<List<ProgressResp>> getProgressBySelection(@PathVariable Long selectionId) {
        return R.ok(progressService.getProgressListBySelection(selectionId));
    }

    /**
     * 学生添加进度记录
     */
    @PostMapping
    @PreAuthorize("hasAuthority('project:progress:add')")
    public R<Void> addProgress(@Valid @RequestBody ProgressAddReq req) {
        progressService.addProgress(req);
        return R.ok();
    }

    /**
     * 更新进度记录
     */
    @PutMapping
    @PreAuthorize("hasAuthority('project:progress:edit')")
    public R<Void> updateProgress(@Valid @RequestBody ProgressUpdateReq req) {
        progressService.updateProgress(req);
        return R.ok();
    }

    /**
     * 删除进度记录
     */
    @DeleteMapping("/{progressId}")
    @PreAuthorize("hasAuthority('project:progress:del')")
    public R<Void> deleteProgress(@PathVariable Long progressId) {
        progressService.deleteProgress(progressId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有通过选题的进度
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('project:progress:list')")
    public R<Page<ProgressResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        ProgressPageReq req = new ProgressPageReq();
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setStudentId(studentId);
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(progressService.pageProgressForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询自己指导学生的进度
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('project:progress:list')")
    public R<Page<ProgressResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        ProgressPageReq req = new ProgressPageReq();
        req.setAcademicYear(academicYear);
        req.setKeyword(keyword);
        return R.ok(progressService.pageProgressForTeacher(req, current, size));
    }
}
