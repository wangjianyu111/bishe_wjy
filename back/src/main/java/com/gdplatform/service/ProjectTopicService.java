package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.TopicAddReq;
import com.gdplatform.dto.TopicResp;
import com.gdplatform.dto.TopicUpdateReq;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectTopicService {

    Page<TopicResp> pageTopics(long current, long size, Long campusId, String campusName, String academicYear, String status, String keyword);

    TopicResp getById(Long topicId);

    void add(TopicAddReq req);

    void update(TopicUpdateReq req);

    void delete(Long topicId);

    void toggleStatus(Long topicId);

    List<TopicResp> export(Long campusId, String campusName, String academicYear, String status, String keyword);

    void importTopics(MultipartFile file);
}
