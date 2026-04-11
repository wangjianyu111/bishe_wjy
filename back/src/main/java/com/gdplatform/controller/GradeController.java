package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.GradeInputReq;
import com.gdplatform.dto.GradeResp;
import com.gdplatform.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/achievement/grade")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    /**
     * 管理员分页查询所有成绩
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Page<GradeResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gradeLevel) {
        return R.ok(gradeService.adminPage(current, size, campusName, academicYear, keyword, gradeLevel));
    }

    /**
     * 教师分页查询（本校学生成绩）
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Page<GradeResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gradeLevel) {
        return R.ok(gradeService.teacherPage(current, size, campusName, academicYear, keyword, gradeLevel));
    }

    /**
     * 学生分页查询（只看自己的成绩）
     */
    @GetMapping("/student/page")
    @PreAuthorize("hasAuthority('achievement:grade:view')")
    public R<Page<GradeResp>> studentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return R.ok(gradeService.studentPage(current, size));
    }

    /**
     * 教师录入/更新单次评分
     */
    @PostMapping
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> inputScore(@Valid @RequestBody GradeInputReq req) {
        gradeService.inputScore(req);
        return R.ok();
    }

    /**
     * 管理员调整最终成绩
     */
    @PutMapping("/adjust")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> adjustScore(
            @RequestParam Long summaryId,
            @RequestParam(required = false) BigDecimal adjustedTotalScore,
            @RequestParam(required = false) String adjustedGradeLevel,
            @RequestParam(required = false) String remark) {
        gradeService.adjustScore(summaryId, adjustedTotalScore, adjustedGradeLevel, remark);
        return R.ok();
    }

    /**
     * 管理员锁定成绩（锁定后教师无法修改）
     */
    @PutMapping("/lock/{summaryId}")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> lockScore(@PathVariable Long summaryId) {
        gradeService.lockScore(summaryId);
        return R.ok();
    }

    /**
     * 管理员解锁成绩
     */
    @PutMapping("/unlock/{summaryId}")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> unlockScore(@PathVariable Long summaryId) {
        gradeService.unlockScore(summaryId);
        return R.ok();
    }

    /**
     * 获取成绩详情（含所有教师评分明细）
     */
    @GetMapping("/{summaryId}")
    @PreAuthorize("hasAnyAuthority('achievement:grade:manage', 'achievement:grade:view')")
    public R<GradeResp> getDetail(@PathVariable Long summaryId) {
        return R.ok(gradeService.getDetail(summaryId));
    }

    /**
     * 删除单条教师评分记录（管理员或记录本人）
     */
    @DeleteMapping("/record/{gradeId}")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> deleteRecord(@PathVariable Long gradeId) {
        gradeService.deleteRecord(gradeId);
        return R.ok();
    }
}
