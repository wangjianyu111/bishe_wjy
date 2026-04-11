package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.*;
import com.gdplatform.service.TeacherFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guidance/feedback")
@RequiredArgsConstructor
public class TeacherFeedbackController {

    private final TeacherFeedbackService feedbackService;

    /**
     * 教师：提交反馈
     */
    @PostMapping
    @PreAuthorize("hasAuthority('guidance:feedback:add')")
    public R<Void> submit(@Valid @RequestBody TeacherFeedbackReq req) {
        feedbackService.submit(req);
        return R.ok();
    }

    /**
     * 管理员：分页查询所有反馈
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('guidance:feedback')")
    public R<Page<TeacherFeedbackResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return R.ok(feedbackService.pageForAdmin(current, size, campusName, academicYear, feedbackType, status, keyword));
    }

    /**
     * 教师：分页查询自己的反馈
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('guidance:feedback:add')")
    public R<Page<TeacherFeedbackResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String status) {
        return R.ok(feedbackService.pageForTeacher(current, size, academicYear, feedbackType, status));
    }

    /**
     * 管理员：处理反馈
     */
    @PutMapping("/handle")
    @PreAuthorize("hasAuthority('guidance:feedback')")
    public R<Void> handle(@RequestBody TeacherFeedbackHandleReq req) {
        feedbackService.handle(req);
        return R.ok();
    }

    /**
     * 管理员：统计报表
     */
    @GetMapping("/stats/admin")
    @PreAuthorize("hasAuthority('guidance:feedback')")
    public R<FeedbackStatsResp> statsForAdmin(
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear) {
        return R.ok(feedbackService.statsForAdmin(campusName, academicYear));
    }

    /**
     * 教师：统计报表
     */
    @GetMapping("/stats/teacher")
    @PreAuthorize("hasAuthority('guidance:feedback:add')")
    public R<FeedbackStatsResp> statsForTeacher(
            @RequestParam(required = false) String academicYear) {
        return R.ok(feedbackService.statsForTeacher(academicYear));
    }
}
