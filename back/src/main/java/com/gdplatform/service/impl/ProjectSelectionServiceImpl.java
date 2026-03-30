package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.SelectionApplyRequest;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.ProjectTopicMapper;
import com.gdplatform.service.ProjectSelectionService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectSelectionServiceImpl implements ProjectSelectionService {

    private final ProjectSelectionMapper projectSelectionMapper;
    private final ProjectTopicMapper projectTopicMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(SelectionApplyRequest request) {
        var user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可申请选题");
        }
        long exists = projectSelectionMapper.selectCount(
                Wrappers.<ProjectSelection>lambdaQuery()
                        .eq(ProjectSelection::getStudentId, user.getUserId())
                        .eq(ProjectSelection::getAcademicYear, request.getAcademicYear()));
        if (exists > 0) {
            throw new BizException("本学年已提交过选题申请");
        }
        ProjectTopic topic = projectTopicMapper.selectById(request.getTopicId());
        if (topic == null) {
            throw new BizException("课题不存在");
        }
        if (!"OPEN".equalsIgnoreCase(topic.getStatus())) {
            throw new BizException("课题未开放选题");
        }
        if (topic.getCurrentCount() != null && topic.getMaxStudents() != null
                && topic.getCurrentCount() >= topic.getMaxStudents()) {
            throw new BizException("课题名额已满");
        }

        ProjectSelection sel = new ProjectSelection();
        sel.setStudentId(user.getUserId());
        sel.setTopicId(request.getTopicId());
        sel.setAcademicYear(request.getAcademicYear());
        sel.setApplyReason(request.getApplyReason());
        sel.setStatus("PENDING");
        projectSelectionMapper.insert(sel);
    }

    @Override
    public Page<ProjectSelection> pageSelections(long current, long size) {
        Page<ProjectSelection> page = new Page<>(current, size);
        return projectSelectionMapper.selectPage(page,
                Wrappers.<ProjectSelection>lambdaQuery()
                        .orderByDesc(ProjectSelection::getCreateTime));
    }
}
