package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.DocArchive;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.DocArchiveMapper;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.DocArchiveService;
import com.gdplatform.service.NotificationService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocArchiveServiceImpl implements DocArchiveService {

    private final DocArchiveMapper archiveMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final DocFileMapper docFileMapper;
    private final NotificationService notificationService;

    @Override
    public void checkSubmissionAllowed(Long selectionId, String stageName) {
        DocArchive existing = archiveMapper.selectBySelectionAndStage(selectionId, stageName);
        if (existing != null && "SUBMITTED".equalsIgnoreCase(existing.getStatus())) {
            throw new BizException("该阶段文档正在待审核中，请等待审核完成后再提交");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(ArchiveSubmitReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可提交归档");
        }
        if (req.getSelectionId() == null) {
            throw new BizException("请先选择选题");
        }
        if (req.getStageName() == null || req.getStageName().isBlank()) {
            throw new BizException("请填写归档阶段名称");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("只能提交自己的归档");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有审批通过的选题才能提交归档");
        }

        checkSubmissionAllowed(req.getSelectionId(), req.getStageName().trim());

        DocArchive record = new DocArchive();
        record.setSelectionId(sel.getSelectionId());
        record.setStudentId(user.getUserId());
        record.setStageName(req.getStageName().trim());
        record.setFileId(req.getFileId());
        record.setRemark(req.getRemark());
        record.setStatus("SUBMITTED");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        archiveMapper.insert(record);

        String topicName = buildTopicName(sel);
        String content = String.format(
            "学生「%s」已提交「%s」归档材料，请及时审核。课题：%s",
            user.getRealName(), req.getStageName().trim(), topicName
        );
        sendNotification(sel.getTeacherId(), "归档材料提交通知", content, "ARCHIVE", record.getArchiveId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(Long archiveId) {
        SysUser user = SecurityUtils.currentUser();
        DocArchive archive = archiveMapper.selectByArchiveId(archiveId);
        if (archive == null) {
            throw new BizException("归档记录不存在");
        }
        if (!archive.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权撤回他人的归档");
        }
        if (!"SUBMITTED".equalsIgnoreCase(archive.getStatus())) {
            throw new BizException("仅可撤回待审核状态的归档");
        }
        if (archive.getFileId() != null) {
            docFileMapper.deleteById(archive.getFileId());
        }
        archiveMapper.deleteByArchiveId(archiveId);
    }

    @Override
    public Page<ArchiveResp> pageForAdmin(ArchivePageReq req, long current, long size) {
        Page<ArchiveResp> page = new Page<>(current, size);
        List<ArchiveResp> records = archiveMapper.selectArchivePage(req, (current - 1) * size, size);
        long total = archiveMapper.countArchivePage(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ArchiveResp> pageForTeacher(ArchivePageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<ArchiveResp> page = new Page<>(current, size);
        List<ArchiveResp> records = archiveMapper.selectArchivePageForTeacher(req, (current - 1) * size, size);
        long total = archiveMapper.countArchivePageForTeacher(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<ArchiveResp> listByStudent(Long selectionId) {
        SysUser user = SecurityUtils.currentUser();
        ArchivePageReq req = new ArchivePageReq();
        req.setStudentId(user.getUserId());
        if (selectionId != null) {
            req.setSelectionId(selectionId);
        }
        List<ArchiveResp> list = archiveMapper.selectArchivePage(req, 0L, 1000L);
        list.forEach(this::enrichStatusLabel);
        return list;
    }

    @Override
    public ArchiveResp getDetail(Long archiveId) {
        DocArchive archive = archiveMapper.selectByArchiveId(archiveId);
        if (archive == null) {
            throw new BizException("归档记录不存在");
        }
        ArchivePageReq req = new ArchivePageReq();
        req.setStudentId(archive.getStudentId());
        List<ArchiveResp> list = archiveMapper.selectArchivePage(req, 0L, 1L);
        ArchiveResp resp = !list.isEmpty() ? list.get(0) : null;
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ArchiveReviewReq req) {
        SysUser inspector = SecurityUtils.currentUser();
        DocArchive archive = archiveMapper.selectByArchiveId(req.getArchiveId());
        if (archive == null) {
            throw new BizException("归档记录不存在");
        }
        if (!"SUBMITTED".equalsIgnoreCase(archive.getStatus())) {
            throw new BizException("该归档已审核，请勿重复操作");
        }
        if (!"PASSED".equalsIgnoreCase(req.getStatus()) && !"FAILED".equalsIgnoreCase(req.getStatus())) {
            throw new BizException("审核结果只能是\"通过\"或\"驳回\"");
        }
        if ("FAILED".equalsIgnoreCase(req.getStatus()) &&
                (req.getReviewComment() == null || req.getReviewComment().isBlank())) {
            throw new BizException("驳回时必须填写审核意见");
        }

        archive.setStatus(req.getStatus().toUpperCase());
        archive.setReviewerId(inspector.getUserId());
        archive.setReviewerName(inspector.getRealName());
        archive.setReviewComment(req.getReviewComment());
        archive.setReviewTime(LocalDateTime.now());
        archive.setUpdateTime(LocalDateTime.now());
        archiveMapper.updateById(archive);

        ProjectSelection sel = selectionMapper.selectById(archive.getSelectionId());
        if (sel != null) {
            String topicName = buildTopicName(sel);
            String title;
            String content;
            if ("PASSED".equalsIgnoreCase(req.getStatus())) {
                title = "归档材料通过通知";
                content = String.format("您的「%s」归档材料已通过审核。课题：%s",
                    archive.getStageName(), topicName);
            } else {
                title = "归档材料驳回通知";
                content = String.format("您的「%s」归档材料未通过审核。驳回原因：%s",
                    archive.getStageName(), req.getReviewComment());
            }
            sendNotification(archive.getStudentId(), title, content, "ARCHIVE", archive.getArchiveId());
        }
    }

    private void enrichStatusLabel(ArchiveResp resp) {
        if (resp.getStatus() == null) {
            resp.setStatusLabel("—");
            return;
        }
        resp.setStatusLabel(switch (resp.getStatus().toUpperCase()) {
            case "SUBMITTED" -> "待审核";
            case "PASSED" -> "已通过";
            case "FAILED" -> "已驳回";
            default -> resp.getStatus();
        });
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String buildTopicName(ProjectSelection sel) {
        if (Boolean.TRUE.equals(sel.getIsCustomTopic())) {
            return sel.getCustomTopicName() != null ? sel.getCustomTopicName() : "自拟课题";
        }
        return "课题";
    }

    private void sendNotification(Long userId, String title, String content, String bizType, Long bizId) {
        if (userId == null) return;
        try {
            com.gdplatform.dto.NotificationAddReq nr = new com.gdplatform.dto.NotificationAddReq();
            nr.setTitle(title);
            nr.setContent(content);
            nr.setNoticeType(2);
            nr.setBizType(bizType);
            nr.setBizId(bizId);
            nr.setReceiverIds(List.of(userId));
            notificationService.add(nr, null);
        } catch (Exception ignored) {
        }
    }
}
