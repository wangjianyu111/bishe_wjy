package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.DocFileService;
import com.gdplatform.service.ProjectMidTermService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project/midterm")
@RequiredArgsConstructor
public class ProjectMidTermController {

    private final ProjectMidTermService midTermService;
    private final DocFileService docFileService;

    // ==================== 学生端 ====================

    /**
     * 学生：获取自己的最新一条中期检查信息
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('project:midterm:submit')")
    public R<MidTermResp> getMyMidTerm() {
        return R.ok(midTermService.getMyMidTerm());
    }

    /**
     * 学生：获取自己的全部中期检查记录（历史列表）
     */
    @GetMapping("/my/list")
    @PreAuthorize("hasAuthority('project:midterm:submit')")
    public R<List<MidTermResp>> getMyMidTermList() {
        return R.ok(midTermService.getMyMidTermList());
    }

    /**
     * 学生：提交中期检查（支持上传附件）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('project:midterm:submit')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam(value = "reportContent", required = false, defaultValue = "") String reportContent,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        MidTermSubmitReq req = new MidTermSubmitReq();
        req.setSelectionId(selectionId);
        req.setReportContent(reportContent);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "MIDTERM", null, selectionId);
            req.setFileId(fileId);
        }
        midTermService.submit(req);
        return R.ok();
    }

    /**
     * 学生：撤回中期检查
     */
    @PutMapping("/recall/{midId}")
    @PreAuthorize("hasAuthority('project:midterm:submit')")
    public R<Void> recall(@PathVariable Long midId) {
        midTermService.recall(midId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有中期检查记录
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('project:midterm:review')")
    public R<Page<MidTermResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        MidTermPageReq req = new MidTermPageReq();
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setStudentId(studentId);
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(midTermService.pageForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询自己指导学生的中期检查
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('project:midterm:review')")
    public R<Page<MidTermResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        MidTermPageReq req = new MidTermPageReq();
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(midTermService.pageForTeacher(req, current, size));
    }

    // ==================== 审核 ====================

    /**
     * 管理员/教师：审核中期检查（通过/驳回）
     */
    @PutMapping("/review")
    @PreAuthorize("hasAuthority('project:midterm:review')")
    public R<Void> review(@Valid @RequestBody MidTermReviewReq req) {
        midTermService.review(req);
        return R.ok();
    }

    /**
     * 管理员/教师：查看中期检查详情
     */
    @GetMapping("/{midId}")
    @PreAuthorize("hasAuthority('project:midterm:review')")
    public R<MidTermResp> getDetail(@PathVariable Long midId) {
        return R.ok(midTermService.getDetail(midId));
    }
}
