package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectMidTerm;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.ProjectMidTermMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.NotificationService;
import com.gdplatform.service.ProjectMidTermService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMidTermServiceImpl implements ProjectMidTermService {

    private final ProjectMidTermMapper midTermMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(MidTermSubmitReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可提交中期检查");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("只能提交自己的中期检查");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有审批通过的选题才能提交中期检查");
        }

        MidTermResp existing = midTermMapper.selectBySelectionId(sel.getSelectionId());
        if (existing != null && !"FAILED".equalsIgnoreCase(existing.getStatus())) {
            throw new BizException("中期检查已提交，请等待审核或撤回后再重新提交");
        }

        // 如果是驳回后重新提交，先删除旧记录
        if (existing != null && "FAILED".equalsIgnoreCase(existing.getStatus())) {
            // 查找并删除旧记录
        }

        ProjectMidTerm record = new ProjectMidTerm();
        record.setSelectionId(sel.getSelectionId());
        record.setReportContent(req.getReportContent());
        record.setFileId(req.getFileId());
        record.setStatus("PENDING");
        record.setCreateTime(LocalDateTime.now());
        midTermMapper.insert(record);

        String topicName = buildTopicName(sel);
        String content = String.format(
            "学生「%s」已提交中期检查报告，请及时审核。课题：%s",
            user.getRealName(), topicName
        );
        sendNotificationToTeacher(sel.getTeacherId(), "中期检查提交通知", content, "MIDTERM", record.getMidId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(Long midId) {
        SysUser user = SecurityUtils.currentUser();
        ProjectMidTerm mid = midTermMapper.selectById(midId);
        if (mid == null) {
            throw new BizException("中期检查记录不存在");
        }

        ProjectSelection sel = selectionMapper.selectById(mid.getSelectionId());
        if (sel == null || !sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权撤回他人的中期检查");
        }
        if (!"PENDING".equalsIgnoreCase(mid.getStatus())) {
            throw new BizException("只有待审核状态的中期检查才能撤回");
        }

        midTermMapper.deleteById(midId);
    }

    @Override
    public MidTermResp getMyMidTerm() {
        SysUser user = SecurityUtils.currentUser();
        LambdaQueryWrapper<ProjectSelection> q = Wrappers.<ProjectSelection>lambdaQuery()
                .eq(ProjectSelection::getStudentId, user.getUserId())
                .eq(ProjectSelection::getStatus, "APPROVED")
                .orderByDesc(ProjectSelection::getCreateTime)
                .last("LIMIT 1");
        ProjectSelection sel = selectionMapper.selectOne(q);
        if (sel == null) {
            return null;
        }
        MidTermResp resp = midTermMapper.selectBySelectionId(sel.getSelectionId());
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    @Override
    public Page<MidTermResp> pageForAdmin(MidTermPageReq req, long current, long size) {
        Page<MidTermResp> page = new Page<>(current, size);
        List<MidTermResp> records = midTermMapper.selectMidTermPage(req, (current - 1) * size, size);
        long total = midTermMapper.countMidTermPage(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<MidTermResp> pageForTeacher(MidTermPageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<MidTermResp> page = new Page<>(current, size);
        List<MidTermResp> records = midTermMapper.selectMidTermPageForTeacher(req, (current - 1) * size, size);
        long total = midTermMapper.countMidTermPageForTeacher(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(MidTermReviewReq req) {
        SysUser inspector = SecurityUtils.currentUser();
        ProjectMidTerm mid = midTermMapper.selectById(req.getMidId());
        if (mid == null) {
            throw new BizException("中期检查记录不存在");
        }
        if (!"PENDING".equalsIgnoreCase(mid.getStatus())) {
            throw new BizException("该中期检查已审核，请勿重复操作");
        }
        if (!"PASSED".equalsIgnoreCase(req.getStatus()) && !"FAILED".equalsIgnoreCase(req.getStatus())) {
            throw new BizException("审核结果只能是\"通过\"或\"驳回\"");
        }
        if ("FAILED".equalsIgnoreCase(req.getStatus()) &&
                (req.getInspectComment() == null || req.getInspectComment().isBlank())) {
            throw new BizException("驳回时必须填写驳回原因");
        }

        mid.setStatus(req.getStatus().toUpperCase());
        mid.setInspectorId(inspector.getUserId());
        mid.setInspectComment(req.getInspectComment());
        mid.setInspectTime(LocalDateTime.now());
        midTermMapper.updateById(mid);

        ProjectSelection sel = selectionMapper.selectById(mid.getSelectionId());
        if (sel != null) {
            String topicName = buildTopicName(sel);
            String title;
            String content;
            if ("PASSED".equalsIgnoreCase(req.getStatus())) {
                title = "中期检查通过通知";
                content = String.format("您的中期检查已通过审核。课题：%s", topicName);
            } else {
                title = "中期检查驳回通知";
                content = String.format("您的中期检查未通过审核。驳回原因：%s", req.getInspectComment());
            }
            sendNotificationToStudent(sel.getStudentId(), title, content, "MIDTERM", mid.getMidId());
        }
    }

    @Override
    public MidTermResp getDetail(Long midId) {
        ProjectMidTerm mid = midTermMapper.selectById(midId);
        if (mid == null) {
            throw new BizException("中期检查记录不存在");
        }
        MidTermResp resp = midTermMapper.selectBySelectionId(mid.getSelectionId());
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    private void enrichStatusLabel(MidTermResp resp) {
        if (resp.getStatus() == null) {
            resp.setStatusLabel("—");
            return;
        }
        resp.setStatusLabel(switch (resp.getStatus().toUpperCase()) {
            case "SUBMITTED" -> "已提交";
            case "PENDING" -> "待审核";
            case "PASSED" -> "已通过";
            case "FAILED" -> "已驳回";
            default -> resp.getStatus();
        });
    }

    private String buildTopicName(ProjectSelection sel) {
        if (Boolean.TRUE.equals(sel.getIsCustomTopic())) {
            return sel.getCustomTopicName() != null ? sel.getCustomTopicName() : "自拟课题";
        }
        return "课题";
    }

    private void sendNotificationToTeacher(Long teacherId, String title, String content, String bizType, Long bizId) {
        if (teacherId == null) return;
        try {
            NotificationAddReq nr = new NotificationAddReq();
            nr.setTitle(title);
            nr.setContent(content);
            nr.setNoticeType(2);
            nr.setBizType(bizType);
            nr.setBizId(bizId);
            nr.setReceiverIds(List.of(teacherId));
            notificationService.add(nr, null);
        } catch (Exception ignored) {
        }
    }

    private void sendNotificationToStudent(Long studentId, String title, String content, String bizType, Long bizId) {
        if (studentId == null) return;
        try {
            NotificationAddReq nr = new NotificationAddReq();
            nr.setTitle(title);
            nr.setContent(content);
            nr.setNoticeType(2);
            nr.setBizType(bizType);
            nr.setBizId(bizId);
            nr.setReceiverIds(List.of(studentId));
            notificationService.add(nr, null);
        } catch (Exception ignored) {
        }
    }
}
