package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.ApprovalOpinionReq;
import com.gdplatform.dto.ApprovalOpinionResp;
import com.gdplatform.service.ApprovalOpinionService;
import com.gdplatform.service.DocFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/achievement/approval")
@RequiredArgsConstructor
public class ApprovalOpinionController {

    private final ApprovalOpinionService approvalOpinionService;
    private final DocFileService docFileService;

    /**
     * 管理员分页查询审批意见
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('achievement:approval:manage')")
    public R<Page<ApprovalOpinionResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String docType) {
        return R.ok(approvalOpinionService.adminPage(current, size, campusName, academicYear, keyword, status, docType));
    }

    /**
     * 教师分页查询审批意见
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('achievement:approval:manage')")
    public R<Page<ApprovalOpinionResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String docType) {
        return R.ok(approvalOpinionService.teacherPage(current, size, academicYear, keyword, status, docType));
    }

    /**
     * 学生查看自己的审批意见
     */
    @GetMapping("/student/page")
    @PreAuthorize("hasAuthority('achievement:approval:view')")
    public R<Page<ApprovalOpinionResp>> studentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String docType) {
        return R.ok(approvalOpinionService.studentPage(current, size, academicYear, status, docType));
    }

    /**
     * 提交审批意见（教师）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('achievement:approval:manage')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam(value = "docId", required = false) Long docId,
            @RequestParam(value = "docType", required = false) String docType,
            @RequestParam(value = "roundNo", required = false, defaultValue = "1") Integer roundNo,
            @RequestParam(value = "score", required = false) Integer score,
            @RequestParam(value = "comment", required = false, defaultValue = "") String comment,
            @RequestParam(value = "status", required = false, defaultValue = "SUBMITTED") String status,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ApprovalOpinionReq req = new ApprovalOpinionReq();
        req.setSelectionId(selectionId);
        req.setDocId(docId);
        req.setDocType(docType);
        req.setRoundNo(roundNo);
        req.setScore(score);
        req.setComment(comment);
        req.setStatus(status);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "APPROVAL_OPINION", null, selectionId);
            req.setFileId(fileId);
        }
        approvalOpinionService.submit(req);
        return R.ok();
    }

    /**
     * 更新审批意见状态（通过/驳回）
     */
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('achievement:approval:manage')")
    public R<Void> updateStatus(@RequestBody ApprovalOpinionReq req) {
        approvalOpinionService.updateStatus(req);
        return R.ok();
    }

    /**
     * 获取审批意见详情
     */
    @GetMapping("/{opinionId}")
    @PreAuthorize("hasAnyAuthority('achievement:approval:manage', 'achievement:approval:view')")
    public R<ApprovalOpinionResp> getDetail(@PathVariable Long opinionId) {
        return R.ok(approvalOpinionService.getDetail(opinionId));
    }

    /**
     * 删除审批意见
     */
    @DeleteMapping("/{opinionId}")
    @PreAuthorize("hasAuthority('achievement:approval:manage')")
    public R<Void> delete(@PathVariable Long opinionId) {
        approvalOpinionService.delete(opinionId);
        return R.ok();
    }
}
