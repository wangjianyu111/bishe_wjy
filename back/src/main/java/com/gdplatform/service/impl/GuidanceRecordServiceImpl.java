package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.GuidanceRecordReq;
import com.gdplatform.dto.GuidanceRecordResp;
import com.gdplatform.dto.GuidanceStatsResp;
import com.gdplatform.dto.SelectionForGuidanceResp;
import com.gdplatform.entity.GuidanceRecord;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.GuidanceRecordMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.GuidanceRecordService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuidanceRecordServiceImpl implements GuidanceRecordService {

    private final GuidanceRecordMapper recordMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<SelectionForGuidanceResp> getSelectionsForTeacher() {
        SysUser teacher = SecurityUtils.currentUser();
        return recordMapper.selectSelectionsForTeacher(teacher.getUserId());
    }

    @Override
    public Page<GuidanceRecordResp> pageForAdmin(long current, long size,
            String campusName, String academicYear, String keyword, Long teacherId) {
        Page<GuidanceRecordResp> page = new Page<>(current, size);
        List<GuidanceRecordResp> records = recordMapper.selectPageForAdmin(
                campusName, academicYear, keyword, teacherId,
                (current - 1) * size, size);
        long total = recordMapper.countPageForAdmin(campusName, academicYear, keyword, teacherId);
        page.setRecords(records);
        page.setTotal(total);
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    public Page<GuidanceRecordResp> pageForTeacher(long current, long size,
            String academicYear, String keyword) {
        SysUser teacher = SecurityUtils.currentUser();
        String campusName = teacher.getCampusName();
        Page<GuidanceRecordResp> page = new Page<>(current, size);
        List<GuidanceRecordResp> records = recordMapper.selectPageForTeacher(
                campusName, academicYear, keyword,
                (current - 1) * size, size);
        long total = recordMapper.countPageForTeacher(campusName, academicYear, keyword);
        page.setRecords(records);
        page.setTotal(total);
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    public Page<GuidanceRecordResp> pageForStudent(long current, long size, String academicYear) {
        SysUser student = SecurityUtils.currentUser();
        Page<GuidanceRecordResp> page = new Page<>(current, size);
        List<GuidanceRecordResp> records = recordMapper.selectPageForStudent(
                student.getUserId(), academicYear,
                (current - 1) * size, size);
        long total = recordMapper.countPageForStudent(student.getUserId(), academicYear);
        page.setRecords(records);
        page.setTotal(total);
        enrichLabels(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(GuidanceRecordReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        if (teacher.getUserType() == null || teacher.getUserType() != 2) {
            throw new BizException("仅指导教师可添加指导记录");
        }
        if (req.getSelectionId() == null) {
            throw new BizException("请选择选题");
        }
        if (req.getGuideTime() == null) {
            throw new BizException("请填写指导时间");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BizException("请填写指导内容");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("该选题尚未审批通过，无法添加指导记录");
        }

        GuidanceRecord record = new GuidanceRecord();
        record.setSelectionId(req.getSelectionId());
        record.setTeacherId(teacher.getUserId());
        record.setStudentId(sel.getStudentId());
        record.setGuideTime(req.getGuideTime());
        record.setPlace(req.getPlace());
        record.setDurationMinutes(req.getDurationMinutes());
        record.setGuidanceType(req.getGuidanceType() != null ? req.getGuidanceType() : "GUIDANCE");
        record.setContent(req.getContent());
        record.setAttachmentId(req.getAttachmentId());
        record.setStatus("PUBLISHED");
        record.setAcademicYear(sel.getAcademicYear());
        recordMapper.insertWithAcademicYear(record);
        return record.getGuideId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GuidanceRecordReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        GuidanceRecord record = recordMapper.selectByGuideId(req.getGuideId());
        if (record == null) {
            throw new BizException("指导记录不存在");
        }
        if (!record.getTeacherId().equals(teacher.getUserId())) {
            throw new BizException("无权修改他人的指导记录");
        }
        if ("PUBLISHED".equalsIgnoreCase(record.getStatus())) {
            record.setStatus("DRAFT");
        }
        record.setGuideTime(req.getGuideTime());
        record.setPlace(req.getPlace());
        record.setDurationMinutes(req.getDurationMinutes());
        record.setGuidanceType(req.getGuidanceType() != null ? req.getGuidanceType() : "GUIDANCE");
        record.setContent(req.getContent());
        record.setAttachmentId(req.getAttachmentId());
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long guideId) {
        SysUser user = SecurityUtils.currentUser();
        GuidanceRecord record = recordMapper.selectByGuideId(guideId);
        if (record == null) {
            throw new BizException("指导记录不存在");
        }
        // 教师只能删除自己的记录，管理员可以删除所有记录
        if (user.getUserType() != 3 && !record.getTeacherId().equals(user.getUserId())) {
            throw new BizException("无权删除他人的指导记录");
        }
        record.setIsDeleted(1);
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFeedback(Long guideId, String feedback) {
        SysUser student = SecurityUtils.currentUser();
        GuidanceRecord record = recordMapper.selectByGuideId(guideId);
        if (record == null) {
            throw new BizException("指导记录不存在");
        }
        if (!record.getStudentId().equals(student.getUserId())) {
            throw new BizException("只能在自己的指导记录下填写反馈");
        }
        record.setStudentFeedback(feedback);
        record.setFeedbackTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    @Override
    public GuidanceRecordResp getDetail(Long guideId) {
        SysUser user = SecurityUtils.currentUser();
        GuidanceRecord record = recordMapper.selectByGuideId(guideId);
        if (record == null) {
            return null;
        }
        // 非管理员只能查看自己相关记录
        if (user.getUserType() != 3
                && !record.getTeacherId().equals(user.getUserId())
                && !record.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权查看此记录");
        }
        GuidanceRecordResp resp = GuidanceRecordResp.builder()
                .guideId(record.getGuideId())
                .selectionId(record.getSelectionId())
                .teacherId(record.getTeacherId())
                .studentId(record.getStudentId())
                .guideTime(record.getGuideTime())
                .place(record.getPlace())
                .durationMinutes(record.getDurationMinutes())
                .guidanceType(record.getGuidanceType())
                .guidanceTypeLabel(labelOf(record.getGuidanceType()))
                .content(record.getContent())
                .attachmentId(record.getAttachmentId())
                .studentFeedback(record.getStudentFeedback())
                .feedbackTime(record.getFeedbackTime())
                .status(record.getStatus())
                .statusLabel("PUBLISHED".equalsIgnoreCase(record.getStatus()) ? "已发布" : "草稿")
                .createTime(record.getCreateTime())
                .totalGuidesForStudent(recordMapper.countByStudentId(record.getStudentId()))
                .build();

        // 补充关联信息
        ProjectSelection sel = selectionMapper.selectById(record.getSelectionId());
        if (sel != null) {
            resp.setTopicName(Boolean.TRUE.equals(sel.getIsCustomTopic()) ? sel.getCustomTopicName() : null);
            resp.setAcademicYear(sel.getAcademicYear());
            resp.setCampusName(sel.getCampusName());
            SysUser teacher = userMapper.selectById(record.getTeacherId());
            if (teacher != null) resp.setTeacherName(teacher.getRealName());
            SysUser stu = userMapper.selectById(record.getStudentId());
            if (stu != null) {
                resp.setStudentName(stu.getRealName());
                resp.setStudentNo(stu.getStudentNo());
            }
        }
        return resp;
    }

    @Override
    public GuidanceStatsResp getStats(Long studentId) {
        if (studentId == null) {
            SysUser student = SecurityUtils.currentUser();
            studentId = student.getUserId();
        }
        int total = recordMapper.countByStudentId(studentId);
        YearMonth now = YearMonth.now();
        String currentYear = String.valueOf(now.getYear());
        String currentMonth = String.format("%d-%02d", now.getYear(), now.getMonthValue());
        int yearlyCount = recordMapper.countByStudentIdAndYear(studentId, currentYear);
        int monthlyCount = 0;
        // 粗略估算月度：用 year+month 过滤（Mapper 目前不支持，改用全量过滤）
        String lastTime = recordMapper.findLastGuideTime(studentId);
        return GuidanceStatsResp.builder()
                .totalCount(total)
                .yearlyCount(yearlyCount)
                .monthlyCount(monthlyCount)
                .lastGuideTime(lastTime)
                .build();
    }

    private void enrichLabels(List<GuidanceRecordResp> records) {
        for (GuidanceRecordResp r : records) {
            r.setGuidanceTypeLabel(labelOf(r.getGuidanceType()));
            r.setStatusLabel("PUBLISHED".equalsIgnoreCase(r.getStatus()) ? "已发布" : "草稿");
        }
    }

    private String labelOf(String type) {
        if (type == null) return "指导";
        return switch (type.toUpperCase()) {
            case "GUIDANCE" -> "指导";
            case "VISIT" -> "走访";
            case "GROUP" -> "座谈";
            case "ONLINE" -> "线上";
            case "OTHER" -> "其他";
            default -> type;
        };
    }
}
