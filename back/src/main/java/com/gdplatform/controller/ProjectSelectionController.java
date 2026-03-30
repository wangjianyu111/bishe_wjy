package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.SelectionApplyRequest;
import com.gdplatform.service.ProjectSelectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project/selections")
@RequiredArgsConstructor
public class ProjectSelectionController {

    private final ProjectSelectionService projectSelectionService;

    @GetMapping
    @PreAuthorize("hasAuthority('project:selection:list')")
    public R<Page<?>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return R.ok(projectSelectionService.pageSelections(current, size));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('project:selection:apply')")
    public R<Void> apply(@Valid @RequestBody SelectionApplyRequest request) {
        projectSelectionService.apply(request);
        return R.ok();
    }
}
