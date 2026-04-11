package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.QualityWarning;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.QualityWarningMapper;
import com.gdplatform.service.QualityWarningService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QualityWarningServiceImpl implements QualityWarningService {

    private final QualityWarningMapper warningMapper;
    private final ProjectSelectionMapper selectionMapper;

    private static final Map<String, String> TYPE_LABELS = Map.ofEntries(
            Map.entry("NO_GUIDANCE", "长期未指导"),
            Map.entry("DOC_DELAY", "文档提交滞后"),
            Map.entry("PROGRESS_DELAY", "进度滞后"),
            Map.entry("LOW_FREQUENCY", "指导频率不足"),
            Map.entry("OTHER", "其他")
    );

    private static final Map<String, String> STATUS_LABELS = Map.ofEntries(
            Map.entry("OPEN", "已开启"),
            Map.entry("CLOSED", "已关闭")
    );

    @Override
    @Transactional
    public void add(WarningReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        if (teacher.getUserType() == null || teacher.getUserType() != 2) {
            throw new BizException("仅指导教师可添加预警");
        }
        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getTeacherId().equals(teacher.getUserId())) {
            throw new BizException("只能为自己的选题添加预警");
        }
        QualityWarning entity = new QualityWarning();
        entity.setSelectionId(req.getSelectionId());
        entity.setTeacherId(teacher.getUserId());
        entity.setStudentId(sel.getStudentId());
        entity.setWarnLevel(req.getWarnLevel());
        entity.setWarnType(req.getWarnType() != null ? req.getWarnType() : "OTHER");
        entity.setReason(req.getReason());
        entity.setStatus("OPEN");
        entity.setAcademicYear(sel.getAcademicYear());
        entity.setCreateTime(LocalDateTime.now());
        warningMapper.insert(entity);
    }

    @Override
    public Page<WarningResp> pageForAdmin(long current, long size,
            String campusName, String academicYear, String warnType,
            Integer warnLevel, String status, String keyword) {
        Page<WarningResp> page = new Page<>(current, size);
        var records = warningMapper.selectPageForAdmin(page, campusName, academicYear,
                warnType, warnLevel, status, keyword);
        page.setRecords(records.getRecords());
        page.setTotal(records.getTotal());
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    public Page<WarningResp> pageForTeacher(long current, long size,
            String academicYear, String warnType,
            Integer warnLevel, String status) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<WarningResp> page = new Page<>(current, size);
        var records = warningMapper.selectPageForTeacher(page, teacher.getUserId(),
                academicYear, warnType, warnLevel, status);
        page.setRecords(records.getRecords());
        page.setTotal(records.getTotal());
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    @Transactional
    public void handle(WarningHandleReq req) {
        SysUser currentUser = SecurityUtils.currentUser();
        QualityWarning w = warningMapper.selectById(req.getWarnId());
        if (w == null) {
            throw new BizException("预警记录不存在");
        }
        w.setStatus("CLOSED");
        w.setHandlerId(currentUser.getUserId());
        w.setHandleComment(req.getHandleComment());
        w.setHandleTime(LocalDateTime.now());
        warningMapper.updateById(w);
    }

    @Override
    public WarningStatsResp statsForAdmin(String campusName, String academicYear) {
        return buildStats(null, null, academicYear);
    }

    @Override
    public WarningStatsResp statsForTeacher(String academicYear) {
        SysUser teacher = SecurityUtils.currentUser();
        return buildStats(teacher.getUserId(), null, academicYear);
    }

    @Override
    @Transactional
    public void delete(Long warnId) {
        SysUser currentUser = SecurityUtils.currentUser();
        QualityWarning w = warningMapper.selectById(warnId);
        if (w == null) {
            throw new BizException("预警记录不存在");
        }
        if (currentUser.getUserType() != 3 && !w.getTeacherId().equals(currentUser.getUserId())) {
            throw new BizException("无权删除他人的预警记录");
        }
        w.setIsDeleted(1);
        w.setUpdateTime(LocalDateTime.now());
        warningMapper.updateById(w);
    }

    private WarningStatsResp buildStats(Long teacherId, String campusName, String academicYear) {
        int total = (int) countBy(teacherId, academicYear, null, null, null);
        int l1 = (int) countBy(teacherId, academicYear, null, 1, null);
        int l2 = (int) countBy(teacherId, academicYear, null, 2, null);
        int l3 = (int) countBy(teacherId, academicYear, null, 3, null);
        int open = (int) countBy(teacherId, academicYear, null, null, "OPEN");
        int closed = (int) countBy(teacherId, academicYear, null, null, "CLOSED");
        int noGuidance = (int) countBy(teacherId, academicYear, "NO_GUIDANCE", null, null);
        int docDelay = (int) countBy(teacherId, academicYear, "DOC_DELAY", null, null);
        int progressDelay = (int) countBy(teacherId, academicYear, "PROGRESS_DELAY", null, null);
        int lowFreq = (int) countBy(teacherId, academicYear, "LOW_FREQUENCY", null, null);
        int other = (int) countBy(teacherId, academicYear, "OTHER", null, null);

        return WarningStatsResp.builder()
                .totalCount(total)
                .levelOneCount(l1)
                .levelTwoCount(l2)
                .levelThreeCount(l3)
                .openCount(open)
                .closedCount(closed)
                .noGuidanceCount(noGuidance)
                .docDelayCount(docDelay)
                .progressDelayCount(progressDelay)
                .lowFrequencyCount(lowFreq)
                .otherCount(other)
                .build();
    }

    private long countBy(Long teacherId, String academicYear, String warnType, Integer warnLevel, String status) {
        LambdaQueryWrapper<QualityWarning> q = new LambdaQueryWrapper<>();
        q.eq(QualityWarning::getIsDeleted, 0);
        if (teacherId != null) q.eq(QualityWarning::getTeacherId, teacherId);
        if (academicYear != null && !academicYear.isEmpty()) q.eq(QualityWarning::getAcademicYear, academicYear);
        if (warnType != null && !warnType.isEmpty()) q.eq(QualityWarning::getWarnType, warnType);
        if (warnLevel != null) q.eq(QualityWarning::getWarnLevel, warnLevel);
        if (status != null && !status.isEmpty()) q.eq(QualityWarning::getStatus, status);
        return warningMapper.selectCount(q);
    }

    private void enrichLabels(java.util.List<WarningResp> records) {
        for (WarningResp r : records) {
            r.setWarnLevelName(levelLabel(r.getWarnLevel()));
            r.setWarnTypeName(TYPE_LABELS.getOrDefault(r.getWarnType(), "其他"));
            r.setStatusName(STATUS_LABELS.getOrDefault(r.getStatus(), r.getStatus()));
        }
    }

    private String levelLabel(Integer level) {
        if (level == null) return "未知";
        return switch (level) {
            case 1 -> "提醒";
            case 2 -> "警告";
            case 3 -> "严重";
            default -> "未知";
        };
    }
}
