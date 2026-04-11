package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.DocArchiveService;
import com.gdplatform.service.DocFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project/doc-archive")
@RequiredArgsConstructor
public class DocArchiveController {

    private final DocArchiveService archiveService;
    private final DocFileService docFileService;

    // ==================== 学生端 ====================

    /**
     * 学生：查询自己的归档列表
     */
    @GetMapping("/my/list")
    @PreAuthorize("hasAuthority('doc:archive:list')")
    public R<List<ArchiveResp>> getMyList(@RequestParam(required = false) Long selectionId) {
        return R.ok(archiveService.listByStudent(selectionId));
    }

    /**
     * 学生：检查是否可以提交（同一阶段待审核时不能提交）
     */
    @GetMapping("/check-submit")
    @PreAuthorize("hasAuthority('doc:archive:submit')")
    public R<Void> checkSubmit(@RequestParam Long selectionId, @RequestParam String stageName) {
        archiveService.checkSubmissionAllowed(selectionId, stageName);
        return R.ok();
    }

    /**
     * 学生：提交归档（支持上传文件）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('doc:archive:submit')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam("stageName") String stageName,
            @RequestParam(required = false, defaultValue = "") String remark,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ArchiveSubmitReq req = new ArchiveSubmitReq();
        req.setSelectionId(selectionId);
        req.setStageName(stageName);
        req.setRemark(remark);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "ARCHIVE", null, selectionId);
            req.setFileId(fileId);
        }
        archiveService.submit(req);
        return R.ok();
    }

    /**
     * 学生：撤回归档
     */
    @PutMapping("/recall/{archiveId}")
    @PreAuthorize("hasAuthority('doc:archive:recall')")
    public R<Void> recall(@PathVariable Long archiveId) {
        archiveService.recall(archiveId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有归档
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('doc:archive:review')")
    public R<Page<ArchiveResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long selectionId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String keyword) {
        ArchivePageReq req = new ArchivePageReq();
        req.setSelectionId(selectionId);
        req.setStudentId(studentId);
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setAcademicYear(academicYear);
        req.setStageName(stageName);
        req.setKeyword(keyword);
        return R.ok(archiveService.pageForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询自己指导学生的归档
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('doc:archive:review')")
    public R<Page<ArchiveResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String keyword) {
        ArchivePageReq req = new ArchivePageReq();
        req.setAcademicYear(academicYear);
        req.setStageName(stageName);
        req.setKeyword(keyword);
        return R.ok(archiveService.pageForTeacher(req, current, size));
    }

    // ==================== 审核 ====================

    /**
     * 管理员/教师：审核归档
     */
    @PutMapping("/review")
    @PreAuthorize("hasAuthority('doc:archive:review')")
    public R<Void> review(@RequestBody ArchiveReviewReq req) {
        archiveService.review(req);
        return R.ok();
    }

    /**
     * 管理员/教师：查看归档详情
     */
    @GetMapping("/{archiveId}")
    @PreAuthorize("hasAuthority('doc:archive:review')")
    public R<ArchiveResp> getDetail(@PathVariable Long archiveId) {
        return R.ok(archiveService.getDetail(archiveId));
    }
}
