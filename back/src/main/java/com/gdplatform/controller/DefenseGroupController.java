package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.DefenseGroupReq;
import com.gdplatform.dto.DefenseGroupResp;
import com.gdplatform.service.DefenseGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guidance/defense-group")
@RequiredArgsConstructor
public class DefenseGroupController {

    private final DefenseGroupService defenseGroupService;

    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('guidance:defense-group:view')")
    public R<Page<DefenseGroupResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(defenseGroupService.pageForTeacher(current, size, academicYear, keyword));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('guidance:defense-group:add')")
    public R<DefenseGroupResp> create(@Valid @RequestBody DefenseGroupReq req) {
        return R.ok(defenseGroupService.create(req));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('guidance:defense-group:edit')")
    public R<Void> update(@Valid @RequestBody DefenseGroupReq req) {
        defenseGroupService.update(req);
        return R.ok();
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAuthority('guidance:defense-group:del')")
    public R<Void> delete(@PathVariable Long groupId) {
        defenseGroupService.delete(groupId);
        return R.ok();
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAuthority('guidance:defense-group:view')")
    public R<DefenseGroupResp> getDetail(@PathVariable Long groupId) {
        return R.ok(defenseGroupService.getDetail(groupId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('guidance:defense-group:view')")
    public R<List<DefenseGroupResp>> myGroups(
            @RequestParam(required = false) String academicYear) {
        return R.ok(defenseGroupService.listByTeacher(null, academicYear));
    }
}
