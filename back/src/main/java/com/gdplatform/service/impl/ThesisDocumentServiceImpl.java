package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ThesisDocument;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.mapper.ThesisDocumentMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.NotificationService;
import com.gdplatform.service.ThesisDocumentService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThesisDocumentServiceImpl implements ThesisDocumentService {

    private final ThesisDocumentMapper thesisMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final DocFileMapper docFileMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(ThesisSubmitReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可提交论文文档");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("只能提交自己的论文文档");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有审批通过的选题才能提交论文文档");
        }

        ThesisResp existing = thesisMapper.selectBySelectionId(sel.getSelectionId());
        if (existing != null && !"FAILED".equalsIgnoreCase(existing.getStatus())) {
            throw new BizException("当前有一份论文正在审核中，请撤回后再提交");
        }

        if (existing != null && "FAILED".equalsIgnoreCase(existing.getStatus())) {
            if (existing.getFileId() != null) {
                docFileMapper.deleteById(existing.getFileId());
            }
            thesisMapper.deleteById(existing.getThesisId());
        }

        Integer newVersion = 1;
        if (existing != null && existing.getVersionNo() != null) {
            newVersion = existing.getVersionNo() + 1;
        }

        ThesisDocument record = new ThesisDocument();
        record.setSelectionId(sel.getSelectionId());
        record.setStudentId(user.getUserId());
        record.setReportContent(req.getReportContent());
        record.setFileId(req.getFileId());
        record.setStatus("PENDING");
        record.setVersionNo(newVersion);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        thesisMapper.insert(record);

        String topicName = buildTopicName(sel);
        String content = String.format(
            "学生「%s」已提交论文文档（第%d版），请及时审核。课题：%s",
            user.getRealName(), newVersion, topicName
        );
        sendNotificationToTeacher(sel.getTeacherId(), "论文文档提交通知", content, "THESIS", record.getThesisId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(Long thesisId) {
        SysUser user = SecurityUtils.currentUser();
        ThesisDocument thesis = thesisMapper.selectById(thesisId);
        if (thesis == null) {
            throw new BizException("论文记录不存在");
        }
        if (!thesis.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权撤回他人的论文");
        }
        if (!"PENDING".equalsIgnoreCase(thesis.getStatus())) {
            throw new BizException("只有待审核状态的论文才能撤回");
        }
        thesisMapper.deleteById(thesisId);
        if (thesis.getFileId() != null) {
            docFileMapper.deleteById(thesis.getFileId());
        }
    }

    @Override
    public ThesisResp getMyThesis() {
        SysUser user = SecurityUtils.currentUser();
        ThesisResp resp = thesisMapper.selectLatestByStudentId(user.getUserId());
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    @Override
    public List<ThesisResp> getMyThesisList() {
        SysUser user = SecurityUtils.currentUser();
        List<ThesisResp> list = thesisMapper.selectAllByStudentId(user.getUserId());
        list.forEach(this::enrichStatusLabel);
        return list;
    }

    @Override
    public Page<ThesisResp> pageForAdmin(ThesisPageReq req, long current, long size) {
        Page<ThesisResp> page = new Page<>(current, size);
        List<ThesisResp> records = thesisMapper.selectThesisPage(req, (current - 1) * size, size);
        long total = thesisMapper.countThesisPage(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ThesisResp> pageForTeacher(ThesisPageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<ThesisResp> page = new Page<>(current, size);
        List<ThesisResp> records = thesisMapper.selectThesisPageForTeacher(req, (current - 1) * size, size);
        long total = thesisMapper.countThesisPageForTeacher(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ThesisReviewReq req) {
        SysUser inspector = SecurityUtils.currentUser();
        ThesisDocument thesis = thesisMapper.selectById(req.getThesisId());
        if (thesis == null) {
            throw new BizException("论文记录不存在");
        }
        if (!"PENDING".equalsIgnoreCase(thesis.getStatus())) {
            throw new BizException("该论文已审核，请勿重复操作");
        }
        if (!"PASSED".equalsIgnoreCase(req.getStatus()) && !"FAILED".equalsIgnoreCase(req.getStatus())) {
            throw new BizException("审核结果只能是\"通过\"或\"驳回\"");
        }
        if ("FAILED".equalsIgnoreCase(req.getStatus()) &&
                (req.getInspectComment() == null || req.getInspectComment().isBlank())) {
            throw new BizException("驳回时必须填写驳回原因");
        }

        thesis.setStatus(req.getStatus().toUpperCase());
        thesis.setInspectorId(inspector.getUserId());
        thesis.setInspectComment(req.getInspectComment());
        thesis.setInspectTime(LocalDateTime.now());
        thesis.setUpdateTime(LocalDateTime.now());
        thesisMapper.updateById(thesis);

        ProjectSelection sel = selectionMapper.selectById(thesis.getSelectionId());
        if (sel != null) {
            String topicName = buildTopicName(sel);
            String title;
            String content;
            if ("PASSED".equalsIgnoreCase(req.getStatus())) {
                title = "论文文档通过通知";
                content = String.format("您的论文文档（第%d版）已通过审核。课题：%s", thesis.getVersionNo(), topicName);
            } else {
                title = "论文文档驳回通知";
                content = String.format("您的论文文档（第%d版）未通过审核。驳回原因：%s", thesis.getVersionNo(), req.getInspectComment());
            }
            sendNotificationToStudent(sel.getStudentId(), title, content, "THESIS", thesis.getThesisId());
        }
    }

    @Override
    public ThesisResp getDetail(Long thesisId) {
        ThesisDocument thesis = thesisMapper.selectById(thesisId);
        if (thesis == null) {
            throw new BizException("论文记录不存在");
        }
        ThesisResp resp = thesisMapper.selectBySelectionId(thesis.getSelectionId());
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    private void enrichStatusLabel(ThesisResp resp) {
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