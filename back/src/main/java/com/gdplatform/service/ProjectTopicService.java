package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.entity.ProjectTopic;

public interface ProjectTopicService {

    Page<ProjectTopic> pageTopics(long current, long size, String academicYear, String status, String keyword);
}
