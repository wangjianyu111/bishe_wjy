package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.DocFileService;
import com.gdplatform.service.DocVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project/doc-version")
@RequiredArgsConstructor
public class DocVersionController {

    private final DocVersionService versionService;
    private final DocFileService docFileService;

    // ==================== 学生端 ====================

    /**
     * 学生：查询自己的文档版本列表
     */
    @GetMapping("/my/list")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<List<VersionResp>> getMyList(@RequestParam(required = false) Long selectionId) {
        return R.ok(versionService.listByStudent(selectionId));
    }

    /**
     * 学生：提交文档版本（支持上传文件）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam("stageName") String stageName,
            @RequestParam(required = false, defaultValue = "1") Integer versionNo,
            @RequestParam(required = false, defaultValue = "") String remark,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        VersionSubmitReq req = new VersionSubmitReq();
        req.setSelectionId(selectionId);
        req.setStageName(stageName);
        req.setVersionNo(versionNo);
        req.setRemark(remark);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "VERSION", null, selectionId);
            req.setFileId(fileId);
        }
        versionService.submit(req);
        return R.ok();
    }

    /**
     * 学生：删除自己的文档版本（仅待审核状态可删）
     */
    @DeleteMapping("/{versionId}")
    @PreAuthorize("hasAuthority('doc:thesis:submit')")
    public R<Void> delete(@PathVariable Long versionId) {
        versionService.deleteByStudent(versionId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有文档版本
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<Page<VersionResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long selectionId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String keyword) {
        VersionPageReq req = new VersionPageReq();
        req.setSelectionId(selectionId);
        req.setStudentId(studentId);
        req.setCampusName(campusName);
        req.setTeacherId(teacherId);
        req.setAcademicYear(academicYear);
        req.setStageName(stageName);
        req.setKeyword(keyword);
        return R.ok(versionService.pageForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询自己指导学生的文档版本
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<Page<VersionResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String keyword) {
        VersionPageReq req = new VersionPageReq();
        req.setAcademicYear(academicYear);
        req.setStageName(stageName);
        req.setKeyword(keyword);
        return R.ok(versionService.pageForTeacher(req, current, size));
    }

    /**
     * 管理员/教师：查看文档版本详情
     */
    @GetMapping("/{versionId}")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<VersionResp> getDetail(@PathVariable Long versionId) {
        return R.ok(versionService.getDetail(versionId));
    }
}
