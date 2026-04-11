package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.entity.TeacherFeedback;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.mapper.TeacherFeedbackMapper;
import com.gdplatform.service.TeacherFeedbackService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherFeedbackServiceImpl implements TeacherFeedbackService {

    private final TeacherFeedbackMapper feedbackMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final SysUserMapper userMapper;

    private static final Map<String, String> TYPE_LABELS = Map.ofEntries(
            Map.entry("TEACHING_QUALITY", "教学质量问题"),
            Map.entry("STUDENT_ISSUE", "学生问题"),
            Map.entry("SYSTEM_IMPROVE", "系统改进建议"),
            Map.entry("RESOURCE_LACK", "资源不足"),
            Map.entry("OTHER", "其他")
    );

    private static final Map<String, String> STATUS_LABELS = Map.ofEntries(
            Map.entry("PENDING", "待处理"),
            Map.entry("HANDLING", "处理中"),
            Map.entry("RESOLVED", "已解决"),
            Map.entry("REJECTED", "已驳回")
    );

    @Override
    @Transactional
    public void submit(TeacherFeedbackReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        TeacherFeedback entity = new TeacherFeedback();
        entity.setSelectionId(req.getSelectionId());
        entity.setTeacherId(teacher.getUserId());
        entity.setFeedbackType(req.getFeedbackType() != null ? req.getFeedbackType() : "OTHER");
        entity.setContent(req.getContent());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel != null) {
            entity.setStudentId(sel.getStudentId());
            entity.setAcademicYear(sel.getAcademicYear());
        }

        feedbackMapper.insert(entity);
    }

    @Override
    public Page<TeacherFeedbackResp> pageForAdmin(long current, long size,
            String campusName, String academicYear, String feedbackType, String status, String keyword) {
        Page<TeacherFeedbackResp> page = new Page<>(current, size);
        List<TeacherFeedbackResp> records = feedbackMapper.selectPageForAdmin(
                campusName, academicYear, feedbackType, status, keyword,
                (current - 1) * size, size);
        long total = feedbackMapper.countPageForAdmin(campusName, academicYear, feedbackType, status, keyword);
        page.setRecords(records);
        page.setTotal(total);
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    public Page<TeacherFeedbackResp> pageForTeacher(long current, long size,
            String academicYear, String feedbackType, String status) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<TeacherFeedbackResp> page = new Page<>(current, size);
        List<TeacherFeedbackResp> records = feedbackMapper.selectPageForTeacher(
                teacher.getUserId(), academicYear, feedbackType, status,
                (current - 1) * size, size);
        long total = feedbackMapper.countPageForTeacher(
                teacher.getUserId(), academicYear, feedbackType, status);
        page.setRecords(records);
        page.setTotal(total);
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    @Transactional
    public void handle(TeacherFeedbackHandleReq req) {
        SysUser currentUser = SecurityUtils.currentUser();
        TeacherFeedback fb = feedbackMapper.selectById(req.getFbId());
        if (fb == null) {
            throw new RuntimeException("反馈记录不存在");
        }
        fb.setStatus(req.getStatus());
        fb.setHandlerId(currentUser.getUserId());
        fb.setHandleComment(req.getHandleComment());
        fb.setHandleTime(LocalDateTime.now());
        feedbackMapper.updateById(fb);
    }

    @Override
    public FeedbackStatsResp statsForAdmin(String campusName, String academicYear) {
        long total = countBy(null, academicYear, null, null);

        int pending = (int) countBy(null, academicYear, null, "PENDING");
        int handling = (int) countBy(null, academicYear, null, "HANDLING");
        int resolved = (int) countBy(null, academicYear, null, "RESOLVED");
        int rejected = (int) countBy(null, academicYear, null, "REJECTED");

        int teaching = (int) countBy(null, academicYear, "TEACHING_QUALITY", null);
        int student = (int) countBy(null, academicYear, "STUDENT_ISSUE", null);
        int system = (int) countBy(null, academicYear, "SYSTEM_IMPROVE", null);
        int resource = (int) countBy(null, academicYear, "RESOURCE_LACK", null);
        int other = (int) countBy(null, academicYear, "OTHER", null);

        return FeedbackStatsResp.builder()
                .totalCount((int) total)
                .pendingCount(pending)
                .handlingCount(handling)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .teachingQualityCount(teaching)
                .studentIssueCount(student)
                .systemImproveCount(system)
                .resourceLackCount(resource)
                .otherCount(other)
                .build();
    }

    @Override
    public FeedbackStatsResp statsForTeacher(String academicYear) {
        SysUser teacher = SecurityUtils.currentUser();
        Long teacherId = teacher.getUserId();

        long total = countBy(teacherId, academicYear, null, null);

        int pending = (int) countBy(teacherId, academicYear, null, "PENDING");
        int handling = (int) countBy(teacherId, academicYear, null, "HANDLING");
        int resolved = (int) countBy(teacherId, academicYear, null, "RESOLVED");
        int rejected = (int) countBy(teacherId, academicYear, null, "REJECTED");

        int teaching = (int) countBy(teacherId, academicYear, "TEACHING_QUALITY", null);
        int student = (int) countBy(teacherId, academicYear, "STUDENT_ISSUE", null);
        int system = (int) countBy(teacherId, academicYear, "SYSTEM_IMPROVE", null);
        int resource = (int) countBy(teacherId, academicYear, "RESOURCE_LACK", null);
        int other = (int) countBy(teacherId, academicYear, "OTHER", null);

        return FeedbackStatsResp.builder()
                .totalCount((int) total)
                .pendingCount(pending)
                .handlingCount(handling)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .teachingQualityCount(teaching)
                .studentIssueCount(student)
                .systemImproveCount(system)
                .resourceLackCount(resource)
                .otherCount(other)
                .build();
    }

    private long countBy(Long teacherId, String academicYear, String feedbackType, String status) {
        LambdaQueryWrapper<TeacherFeedback> q = new LambdaQueryWrapper<>();
        q.eq(TeacherFeedback::getIsDeleted, 0);
        if (teacherId != null) q.eq(TeacherFeedback::getTeacherId, teacherId);
        if (academicYear != null && !academicYear.isEmpty()) q.eq(TeacherFeedback::getAcademicYear, academicYear);
        if (feedbackType != null && !feedbackType.isEmpty()) q.eq(TeacherFeedback::getFeedbackType, feedbackType);
        if (status != null && !status.isEmpty()) q.eq(TeacherFeedback::getStatus, status);
        return feedbackMapper.selectCount(q);
    }

    private void enrichLabels(List<TeacherFeedbackResp> records) {
        for (TeacherFeedbackResp r : records) {
            r.setFeedbackTypeName(TYPE_LABELS.getOrDefault(r.getFeedbackType(), "其他"));
            r.setStatusName(STATUS_LABELS.getOrDefault(r.getStatus(), r.getStatus()));
        }
    }
}
