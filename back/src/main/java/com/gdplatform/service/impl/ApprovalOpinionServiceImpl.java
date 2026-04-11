package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.ApprovalOpinionReq;
import com.gdplatform.dto.ApprovalOpinionResp;
import com.gdplatform.dto.NotificationAddReq;
import com.gdplatform.entity.ApprovalOpinion;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.ApprovalOpinionMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.ApprovalOpinionService;
import com.gdplatform.service.NotificationService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalOpinionServiceImpl implements ApprovalOpinionService {

    private final ApprovalOpinionMapper opinionMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final NotificationService notificationService;

    @Override
    public Page<ApprovalOpinionResp> adminPage(long current, long size, String campusName,
                                               String academicYear, String keyword, String status, String docType) {
        Page<ApprovalOpinionResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ApprovalOpinionResp> records = opinionMapper.selectAdminPage(
                campusName, academicYear, keyword, status, docType, offset, size);
        long total = opinionMapper.countAdminPage(campusName, academicYear, keyword, status, docType);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ApprovalOpinionResp> teacherPage(long current, long size, String academicYear,
                                                  String keyword, String status, String docType) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<ApprovalOpinionResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ApprovalOpinionResp> records = opinionMapper.selectTeacherPage(
                teacher.getUserId(), academicYear, keyword, status, docType, offset, size);
        long total = opinionMapper.countTeacherPage(
                teacher.getUserId(), academicYear, keyword, status, docType);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ApprovalOpinionResp> studentPage(long current, long size, String academicYear,
                                                  String status, String docType) {
        SysUser student = SecurityUtils.currentUser();
        Page<ApprovalOpinionResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ApprovalOpinionResp> records = opinionMapper.selectStudentPage(
                student.getUserId(), academicYear, status, docType, offset, size);
        long total = opinionMapper.countStudentPage(
                student.getUserId(), academicYear, status, docType);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApprovalOpinionReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        if (teacher.getUserType() == null || teacher.getUserType() != 2) {
            throw new BizException("只有教师才能提交审批意见");
        }
        if (req.getSelectionId() == null) {
            throw new BizException("请选择关联的选题");
        }
        if (req.getRoundNo() == null) {
            req.setRoundNo(1);
        }

        // 计算轮次
        Integer round = req.getRoundNo();
        if (round == null || round <= 0) {
            round = 1;
        }

        ApprovalOpinion entity = new ApprovalOpinion();
        entity.setSelectionId(req.getSelectionId());
        entity.setDocId(req.getDocId());
        entity.setDocType(req.getDocType());
        entity.setRoundNo(round);
        entity.setReviewerId(teacher.getUserId());
        entity.setScore(req.getScore());
        entity.setComment(req.getComment());
        entity.setFileId(req.getFileId());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "SUBMITTED");
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        opinionMapper.insert(entity);

        // 通知学生
        notifyStudent(entity);

        return entity.getOpinionId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ApprovalOpinionReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        if (req.getOpinionId() == null) {
            throw new BizException("审批意见ID不能为空");
        }
        ApprovalOpinion entity = opinionMapper.selectById(req.getOpinionId());
        if (entity == null) {
            throw new BizException("审批意见不存在");
        }
        // 非管理员只能更新自己的意见
        if (teacher.getUserType() != 3 && !entity.getReviewerId().equals(teacher.getUserId())) {
            throw new BizException("无权修改此审批意见");
        }
        if (req.getScore() != null) {
            entity.setScore(req.getScore());
        }
        if (req.getComment() != null) {
            entity.setComment(req.getComment());
        }
        if (req.getStatus() != null) {
            entity.setStatus(req.getStatus());
        }
        entity.setUpdateTime(LocalDateTime.now());
        opinionMapper.updateById(entity);

        // 通知学生
        notifyStudent(entity);
    }

    @Override
    public ApprovalOpinionResp getDetail(Long opinionId) {
        ApprovalOpinion entity = opinionMapper.selectById(opinionId);
        if (entity == null) {
            throw new BizException("审批意见不存在");
        }
        SysUser teacher = SecurityUtils.currentUser();
        // 学生只能查看自己的意见
        if (teacher.getUserType() == 1) {
            // 暂时不限制，学生通过其他接口访问
        }
        ApprovalOpinionResp resp = convertToResp(entity);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long opinionId) {
        SysUser teacher = SecurityUtils.currentUser();
        ApprovalOpinion entity = opinionMapper.selectById(opinionId);
        if (entity == null) {
            throw new BizException("审批意见不存在");
        }
        // 非管理员只能删除自己的意见
        if (teacher.getUserType() != 3 && !entity.getReviewerId().equals(teacher.getUserId())) {
            throw new BizException("无权删除此审批意见");
        }
        opinionMapper.deleteById(opinionId);
    }

    private void notifyStudent(ApprovalOpinion entity) {
        try {
            ProjectSelection sel = selectionMapper.selectById(entity.getSelectionId());
            if (sel == null) return;
            Long studentId = sel.getStudentId();
            if (studentId == null) return;

            NotificationAddReq notification = new NotificationAddReq();
            notification.setTitle("您有新的审批意见");
            String docTypeLabel = switch (entity.getDocType()) {
                case "PROPOSAL" -> "开题报告";
                case "MIDTERM" -> "中期检查";
                case "THESIS" -> "论文文档";
                case "ARCHIVE" -> "文档归档";
                default -> entity.getDocType() != null ? entity.getDocType() : "文档";
            };
            String comment = entity.getComment();
            String preview = (comment != null && comment.length() > 50) ? comment.substring(0, 50) + "..." : (comment != null ? comment : "无");
            notification.setContent(String.format(
                    "教师对您的%s（第%d轮）发布了审批意见：\n%s\n请登录系统查看详情。",
                    docTypeLabel, entity.getRoundNo(), preview));
            notification.setNoticeType(1);
            notification.setBizType("APPROVAL_OPINION");
            notification.setBizId(entity.getOpinionId());
            notification.setReceiverIds(List.of(studentId));
            notificationService.add(notification, entity.getReviewerId());
        } catch (Exception ignored) {
        }
    }

    private ApprovalOpinionResp convertToResp(ApprovalOpinion entity) {
        ApprovalOpinionResp resp = new ApprovalOpinionResp();
        resp.setOpinionId(entity.getOpinionId());
        resp.setSelectionId(entity.getSelectionId());
        resp.setDocId(entity.getDocId());
        resp.setDocType(entity.getDocType());
        resp.setRoundNo(entity.getRoundNo());
        resp.setReviewerId(entity.getReviewerId());
        resp.setScore(entity.getScore());
        resp.setComment(entity.getComment());
        resp.setFileId(entity.getFileId());
        resp.setStatus(entity.getStatus());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }
}
