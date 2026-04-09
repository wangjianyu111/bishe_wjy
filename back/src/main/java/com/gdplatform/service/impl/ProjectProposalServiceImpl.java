package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectProposal;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.mapper.ProjectProposalMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.NotificationService;
import com.gdplatform.service.ProjectProposalService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectProposalServiceImpl implements ProjectProposalService {

    private final ProjectProposalMapper proposalMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final DocFileMapper docFileMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(ProposalSubmitReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可提交开题报告");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("只能提交自己的开题报告");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有审批通过的选题才能提交开题报告");
        }

        ProposalResp existing = proposalMapper.selectBySelectionId(sel.getSelectionId());
        if (existing != null && !"FAILED".equalsIgnoreCase(existing.getStatus())) {
            throw new BizException("开题报告已提交，请等待审核或撤回后再重新提交");
        }

        if (existing != null && "FAILED".equalsIgnoreCase(existing.getStatus())) {
            if (existing.getFileId() != null) {
                docFileMapper.deleteById(existing.getFileId());
            }
            proposalMapper.deleteById(existing.getProposalId());
        }

        ProjectProposal record = new ProjectProposal();
        record.setSelectionId(sel.getSelectionId());
        record.setReportContent(req.getReportContent());
        record.setFileId(req.getFileId());
        record.setStatus("PENDING");
        record.setCreateTime(LocalDateTime.now());
        proposalMapper.insert(record);

        String topicName = buildTopicName(sel);
        String content = String.format(
            "学生「%s」已提交开题报告，请及时审核。课题：%s",
            user.getRealName(), topicName
        );
        sendNotificationToTeacher(sel.getTeacherId(), "开题报告提交通知", content, "PROPOSAL", record.getProposalId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(Long proposalId) {
        SysUser user = SecurityUtils.currentUser();
        ProjectProposal pro = proposalMapper.selectById(proposalId);
        if (pro == null) {
            throw new BizException("开题报告记录不存在");
        }

        ProjectSelection sel = selectionMapper.selectById(pro.getSelectionId());
        if (sel == null || !sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权撤回他人的开题报告");
        }
        if (!"PENDING".equalsIgnoreCase(pro.getStatus())) {
            throw new BizException("只有待审核状态的开题报告才能撤回");
        }

        proposalMapper.deleteById(proposalId);
    }

    @Override
    public ProposalResp getMyProposal() {
        SysUser user = SecurityUtils.currentUser();
        ProposalResp resp = proposalMapper.selectLatestByStudentId(user.getUserId());
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    @Override
    public List<ProposalResp> getMyProposalList() {
        SysUser user = SecurityUtils.currentUser();
        List<ProposalResp> list = proposalMapper.selectAllByStudentId(user.getUserId());
        list.forEach(this::enrichStatusLabel);
        return list;
    }

    @Override
    public Page<ProposalResp> pageForAdmin(ProposalPageReq req, long current, long size) {
        Page<ProposalResp> page = new Page<>(current, size);
        List<ProposalResp> records = proposalMapper.selectProposalPage(req, (current - 1) * size, size);
        long total = proposalMapper.countProposalPage(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ProposalResp> pageForTeacher(ProposalPageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<ProposalResp> page = new Page<>(current, size);
        List<ProposalResp> records = proposalMapper.selectProposalPageForTeacher(req, (current - 1) * size, size);
        long total = proposalMapper.countProposalPageForTeacher(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ProposalReviewReq req) {
        SysUser inspector = SecurityUtils.currentUser();
        ProjectProposal pro = proposalMapper.selectById(req.getProposalId());
        if (pro == null) {
            throw new BizException("开题报告记录不存在");
        }
        if (!"PENDING".equalsIgnoreCase(pro.getStatus())) {
            throw new BizException("该开题报告已审核，请勿重复操作");
        }
        if (!"PASSED".equalsIgnoreCase(req.getStatus()) && !"FAILED".equalsIgnoreCase(req.getStatus())) {
            throw new BizException("审核结果只能是\"通过\"或\"驳回\"");
        }
        if ("FAILED".equalsIgnoreCase(req.getStatus()) &&
                (req.getInspectComment() == null || req.getInspectComment().isBlank())) {
            throw new BizException("驳回时必须填写驳回原因");
        }

        pro.setStatus(req.getStatus().toUpperCase());
        pro.setInspectorId(inspector.getUserId());
        pro.setInspectComment(req.getInspectComment());
        pro.setInspectTime(LocalDateTime.now());
        proposalMapper.updateById(pro);

        ProjectSelection sel = selectionMapper.selectById(pro.getSelectionId());
        if (sel != null) {
            String topicName = buildTopicName(sel);
            String title;
            String content;
            if ("PASSED".equalsIgnoreCase(req.getStatus())) {
                title = "开题报告通过通知";
                content = String.format("您的开题报告已通过审核。课题：%s", topicName);
            } else {
                title = "开题报告驳回通知";
                content = String.format("您的开题报告未通过审核。驳回原因：%s", req.getInspectComment());
            }
            sendNotificationToStudent(sel.getStudentId(), title, content, "PROPOSAL", pro.getProposalId());
        }
    }

    @Override
    public ProposalResp getDetail(Long proposalId) {
        ProjectProposal pro = proposalMapper.selectById(proposalId);
        if (pro == null) {
            throw new BizException("开题报告记录不存在");
        }
        ProposalResp resp = proposalMapper.selectBySelectionId(pro.getSelectionId());
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    private void enrichStatusLabel(ProposalResp resp) {
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
