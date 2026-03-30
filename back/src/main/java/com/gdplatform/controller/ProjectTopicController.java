package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.service.ProjectTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project/topics")
@RequiredArgsConstructor
public class ProjectTopicController {

    private final ProjectTopicService projectTopicService;

    @GetMapping
    @PreAuthorize("hasAuthority('project:topic:list')")
    public R<Page<ProjectTopic>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return R.ok(projectTopicService.pageTopics(current, size, academicYear, status, keyword));
    }
}
