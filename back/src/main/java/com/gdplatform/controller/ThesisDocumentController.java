package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.DocFileService;
import com.gdplatform.service.ThesisDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project/thesis")
@RequiredArgsConstructor
public class ThesisDocumentController {

    private final ThesisDocumentService thesisService;
    private final DocFileService docFileService;

    // ==================== 学生端 ====================

    /**
     * 学生：获取自己的最新一条论文文档信息
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<ThesisResp> getMyThesis() {
        return R.ok(thesisService.getMyThesis());
    }

    /**
     * 学生：获取自己的全部论文文档记录（历史列表）
     */
    @GetMapping("/my/list")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<List<ThesisResp>> getMyThesisList() {
        return R.ok(thesisService.getMyThesisList());
    }

    /**
     * 学生：提交论文文档（支持上传附件）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam(value = "reportContent", required = false, defaultValue = "") String reportContent,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ThesisSubmitReq req = new ThesisSubmitReq();
        req.setSelectionId(selectionId);
        req.setReportContent(reportContent);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "THESIS", null, selectionId);
            req.setFileId(fileId);
        }
        thesisService.submit(req);
        return R.ok();
    }

    /**
     * 学生：撤回论文文档
     */
    @PutMapping("/recall/{thesisId}")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<Void> recall(@PathVariable Long thesisId) {
        thesisService.recall(thesisId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有论文文档记录
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<Page<ThesisResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        ThesisPageReq req = new ThesisPageReq();
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setStudentId(studentId);
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(thesisService.pageForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询自己指导学生的论文文档
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<Page<ThesisResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        ThesisPageReq req = new ThesisPageReq();
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(thesisService.pageForTeacher(req, current, size));
    }

    // ==================== 审核 ====================

    /**
     * 管理员/教师：审核论文文档（通过/驳回）
     */
    @PutMapping("/review")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<Void> review(@RequestBody ThesisReviewReq req) {
        thesisService.review(req);
        return R.ok();
    }

    /**
     * 管理员/教师：查看论文文档详情
     */
    @GetMapping("/{thesisId}")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<ThesisResp> getDetail(@PathVariable Long thesisId) {
        return R.ok(thesisService.getDetail(thesisId));
    }
}