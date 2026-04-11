package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.GuidanceFeedbackReq;
import com.gdplatform.dto.GuidanceRecordReq;
import com.gdplatform.dto.GuidanceRecordResp;
import com.gdplatform.dto.GuidanceStatsResp;
import com.gdplatform.dto.SelectionForGuidanceResp;
import com.gdplatform.service.GuidanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guidance/record")
@RequiredArgsConstructor
public class GuidanceRecordController {

    private final GuidanceRecordService recordService;

    /**
     * 教师：查询可添加指导的选题列表（来自指导关系）
     */
    @GetMapping("/selections")
    @PreAuthorize("hasAuthority('guidance:record:add')")
    public R<List<SelectionForGuidanceResp>> getSelectionsForTeacher() {
        return R.ok(recordService.getSelectionsForTeacher());
    }

    /**
     * 管理员：分页查询所有指导记录
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('guidance:record:view')")
    public R<Page<GuidanceRecordResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long teacherId) {
        return R.ok(recordService.pageForAdmin(current, size, campusName, academicYear, keyword, teacherId));
    }

    /**
     * 教师：分页查询（本校学生的指导记录）
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('guidance:record:view')")
    public R<Page<GuidanceRecordResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(recordService.pageForTeacher(current, size, academicYear, keyword));
    }

    /**
     * 学生：分页查询（自己的指导记录）
     */
    @GetMapping("/student/page")
    @PreAuthorize("hasAuthority('guidance:record:view')")
    public R<Page<GuidanceRecordResp>> studentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear) {
        return R.ok(recordService.pageForStudent(current, size, academicYear));
    }

    /**
     * 教师：添加指导记录
     */
    @PostMapping
    @PreAuthorize("hasAuthority('guidance:record:add')")
    public R<Long> add(@Valid @RequestBody GuidanceRecordReq req) {
        return R.ok(recordService.add(req));
    }

    /**
     * 教师：更新指导记录
     */
    @PutMapping
    @PreAuthorize("hasAuthority('guidance:record:edit')")
    public R<Void> update(@Valid @RequestBody GuidanceRecordReq req) {
        recordService.update(req);
        return R.ok();
    }

    /**
     * 删除指导记录
     */
    @DeleteMapping("/{guideId}")
    @PreAuthorize("hasAuthority('guidance:record:del')")
    public R<Void> delete(@PathVariable Long guideId) {
        recordService.delete(guideId);
        return R.ok();
    }

    /**
     * 学生：填写反馈
     */
    @PostMapping("/feedback/{guideId}")
    @PreAuthorize("hasAuthority('guidance:record:feedback')")
    public R<Void> addFeedback(@PathVariable Long guideId, @RequestBody GuidanceFeedbackReq req) {
        recordService.addFeedback(guideId, req.getFeedback());
        return R.ok();
    }

    /**
     * 查看详情
     */
    @GetMapping("/{guideId}")
    @PreAuthorize("hasAuthority('guidance:record:view')")
    public R<GuidanceRecordResp> getDetail(@PathVariable Long guideId) {
        return R.ok(recordService.getDetail(guideId));
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('guidance:record:view')")
    public R<GuidanceStatsResp> getStats(@RequestParam(required = false) Long studentId) {
        return R.ok(recordService.getStats(studentId));
    }
}
