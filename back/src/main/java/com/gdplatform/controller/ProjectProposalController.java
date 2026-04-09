package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.DocFileService;
import com.gdplatform.service.ProjectProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project/proposal")
@RequiredArgsConstructor
public class ProjectProposalController {

    private final ProjectProposalService proposalService;
    private final DocFileService docFileService;

    // ==================== 学生端 ====================

    /**
     * 学生：获取自己的最新一条开题报告信息
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('doc:proposal:submit')")
    public R<ProposalResp> getMyProposal() {
        return R.ok(proposalService.getMyProposal());
    }

    /**
     * 学生：获取自己的全部开题报告记录（历史列表）
     */
    @GetMapping("/my/list")
    @PreAuthorize("hasAuthority('doc:proposal:submit')")
    public R<List<ProposalResp>> getMyProposalList() {
        return R.ok(proposalService.getMyProposalList());
    }

    /**
     * 学生：提交开题报告（支持上传附件）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('doc:proposal:submit')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam(value = "reportContent", required = false, defaultValue = "") String reportContent,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ProposalSubmitReq req = new ProposalSubmitReq();
        req.setSelectionId(selectionId);
        req.setReportContent(reportContent);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "PROPOSAL", null, selectionId);
            req.setFileId(fileId);
        }
        proposalService.submit(req);
        return R.ok();
    }

    /**
     * 学生：撤回开题报告
     */
    @PutMapping("/recall/{proposalId}")
    @PreAuthorize("hasAuthority('doc:proposal:submit')")
    public R<Void> recall(@PathVariable Long proposalId) {
        proposalService.recall(proposalId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有开题报告记录
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('doc:proposal:review')")
    public R<Page<ProposalResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        ProposalPageReq req = new ProposalPageReq();
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setStudentId(studentId);
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(proposalService.pageForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询自己指导学生的开题报告
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('doc:proposal:review')")
    public R<Page<ProposalResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        ProposalPageReq req = new ProposalPageReq();
        req.setAcademicYear(academicYear);
        req.setStatus(status);
        req.setKeyword(keyword);
        return R.ok(proposalService.pageForTeacher(req, current, size));
    }

    // ==================== 审核 ====================

    /**
     * 管理员/教师：审核开题报告（通过/驳回）
     */
    @PutMapping("/review")
    @PreAuthorize("hasAuthority('doc:proposal:review')")
    public R<Void> review(@Valid @RequestBody ProposalReviewReq req) {
        proposalService.review(req);
        return R.ok();
    }

    /**
     * 管理员/教师：查看开题报告详情
     */
    @GetMapping("/{proposalId}")
    @PreAuthorize("hasAuthority('doc:proposal:review')")
    public R<ProposalResp> getDetail(@PathVariable Long proposalId) {
        return R.ok(proposalService.getDetail(proposalId));
    }
}
