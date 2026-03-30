package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.mapper.ProjectTopicMapper;
import com.gdplatform.service.ProjectTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProjectTopicServiceImpl implements ProjectTopicService {

    private final ProjectTopicMapper projectTopicMapper;

    @Override
    public Page<ProjectTopic> pageTopics(long current, long size, String academicYear, String status, String keyword) {
        Page<ProjectTopic> page = new Page<>(current, size);
        return projectTopicMapper.selectPage(page, Wrappers.<ProjectTopic>lambdaQuery()
                .eq(StringUtils.hasText(academicYear), ProjectTopic::getAcademicYear, academicYear)
                .eq(StringUtils.hasText(status), ProjectTopic::getStatus, status)
                .like(StringUtils.hasText(keyword), ProjectTopic::getTopicName, keyword)
                .orderByDesc(ProjectTopic::getCreateTime));
    }
}
