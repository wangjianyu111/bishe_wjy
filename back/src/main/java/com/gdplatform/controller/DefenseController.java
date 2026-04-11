package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.DefenseArrangeResp;
import com.gdplatform.dto.DefenseSessionReq;
import com.gdplatform.dto.DefenseSessionResp;
import com.gdplatform.service.DefenseService;
import com.gdplatform.service.DocFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/achievement/defense")
@RequiredArgsConstructor
public class DefenseController {

    private final DefenseService defenseService;
    private final DocFileService docFileService;

    /**
     * 教师/管理员：发布答辩安排
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('achievement:defense:arrange')")
    public R<Void> publish(
            @RequestParam("sessionName") String sessionName,
            @RequestParam("defenseDate") String defenseDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam("defenseForm") String defenseForm,
            @RequestParam("academicYear") String academicYear,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "remark", required = false, defaultValue = "") String remark) {
        DefenseSessionReq req = new DefenseSessionReq();
        req.setSessionName(sessionName);
        req.setDefenseDate(java.time.LocalDate.parse(defenseDate));
        req.setStartTime(java.time.LocalTime.parse(startTime));
        req.setEndTime(java.time.LocalTime.parse(endTime));
        req.setLocation(location);
        req.setDefenseForm(defenseForm);
        req.setAcademicYear(academicYear);
        req.setRemark(remark);
        if (file != null && !file.isEmpty()) {
            Long fileId = docFileService.uploadFile(file, "DEFENSE", null, null);
            req.setFileId(fileId);
        }
        defenseService.publish(req);
        return R.ok();
    }

    /**
     * 管理员：分页查询所有答辩安排
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('achievement:defense:arrange')")
    public R<Page<DefenseSessionResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(defenseService.pageForAdmin(current, size, campusName, academicYear, keyword));
    }

    /**
     * 教师：分页查询本校答辩安排
     */
    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('achievement:defense:arrange')")
    public R<Page<DefenseSessionResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(defenseService.pageForTeacher(current, size, academicYear, keyword));
    }

    /**
     * 学生：分页查询自己的答辩安排
     */
    @GetMapping("/student/page")
    @PreAuthorize("hasAuthority('achievement:defense:list')")
    public R<Page<DefenseSessionResp>> studentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear) {
        return R.ok(defenseService.pageForStudent(current, size, academicYear));
    }

    /**
     * 管理员：查询答辩安排明细
     */
    @GetMapping("/admin/arrangement/page")
    @PreAuthorize("hasAuthority('achievement:defense:arrange')")
    public R<Page<DefenseArrangeResp>> arrangementPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String keyword) {
        return R.ok(defenseService.arrangementPageForAdmin(current, size, sessionId, keyword));
    }

    /**
     * 查看答辩场次详情
     */
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('achievement:defense:list')")
    public R<DefenseSessionResp> getDetail(@PathVariable Long sessionId) {
        return R.ok(defenseService.getSessionDetail(sessionId));
    }
}
