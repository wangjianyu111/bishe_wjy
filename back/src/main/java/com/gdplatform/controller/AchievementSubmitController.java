package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.AchievementSubmitService;
import com.gdplatform.service.DocFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/achievement/submit")
@RequiredArgsConstructor
public class AchievementSubmitController {

    private final AchievementSubmitService submitService;
    private final DocFileService docFileService;

    /**
     * 学生：获取我的成果提交记录
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('achievement:submit:list')")
    public R<AchievementResp> getMySubmission(@RequestParam(required = false) Long selectionId) {
        if (selectionId == null) {
            List<AchievementResp> list = submitService.listByStudent(null);
            if (list.isEmpty()) {
                return R.ok(null);
            }
            return R.ok(list.get(0));
        }
        return R.ok(submitService.getMySubmission(selectionId));
    }

    /**
     * 学生：提交成果（上传压缩包）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('achievement:submit:upload')")
    public R<Void> submit(
            @RequestParam("selectionId") Long selectionId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "remark", required = false, defaultValue = "") String remark) {
        AchievementSubmitReq req = new AchievementSubmitReq();
        req.setSelectionId(selectionId);
        req.setRemark(remark);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "ACHIEVEMENT", null, selectionId);
            req.setFileId(fileId);
        }
        submitService.submit(req);
        return R.ok();
    }

    /**
     * 学生：删除成果
     */
    @DeleteMapping("/{submitId}")
    @PreAuthorize("hasAuthority('achievement:submit:del')")
    public R<Void> delete(@PathVariable Long submitId) {
        submitService.deleteByStudent(submitId);
        return R.ok();
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员：分页查询所有成果提交
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('achievement:submit:list')")
    public R<Page<AchievementResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long selectionId,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        AchievementPageReq req = new AchievementPageReq();
        req.setSelectionId(selectionId);
        req.setCampusName(campusName);
        req.setAcademicYear(academicYear);
        req.setKeyword(keyword);
        return R.ok(submitService.pageForAdmin(req, current, size));
    }

    // ==================== 教师端 ====================

    /**
     * 教师：分页查询本校学生的成果提交
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('achievement:submit:list')")
    public R<Page<AchievementResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        AchievementPageReq req = new AchievementPageReq();
        req.setAcademicYear(academicYear);
        req.setKeyword(keyword);
        return R.ok(submitService.pageForTeacher(req, current, size));
    }

    /**
     * 查看详情
     */
    @GetMapping("/{submitId}")
    @PreAuthorize("hasAuthority('achievement:submit:list')")
    public R<AchievementResp> getDetail(@PathVariable Long submitId) {
        return R.ok(submitService.getDetail(submitId));
    }
}
