package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.DefenseArrangement;
import com.gdplatform.entity.DefenseSession;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.*;
import com.gdplatform.service.DefenseService;
import com.gdplatform.service.NotificationService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefenseServiceImpl implements DefenseService {

    private final DefenseSessionMapper sessionMapper;
    private final DefenseArrangementMapper arrangeMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;
    private final ProjectSelectionMapper selectionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(DefenseSessionReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        if (teacher.getUserType() == null || teacher.getUserType() != 2) {
            throw new BizException("只有教师才能发布答辩安排");
        }
        if (req.getSessionName() == null || req.getSessionName().trim().isEmpty()) {
            throw new BizException("答辩场次名称不能为空");
        }
        if (req.getDefenseDate() == null) {
            throw new BizException("答辩日期不能为空");
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new BizException("答辩时间不能为空");
        }
        if (req.getAcademicYear() == null || req.getAcademicYear().trim().isEmpty()) {
            throw new BizException("学年不能为空");
        }

        DefenseSession session = new DefenseSession();
        session.setSessionName(req.getSessionName());
        session.setDefenseDate(req.getDefenseDate());
        session.setStartTime(req.getStartTime());
        session.setEndTime(req.getEndTime());
        session.setLocation(req.getLocation());
        session.setDefenseForm(req.getDefenseForm());
        session.setAcademicYear(req.getAcademicYear());
        session.setFileId(req.getFileId());
        session.setRemark(req.getRemark());
        session.setTeacherId(teacher.getUserId());
        session.setCampusName(teacher.getCampusName());
        session.setCreateTime(LocalDateTime.now());
        sessionMapper.insert(session);

        List<Long> studentIds = userMapper.selectStudentIdsByCampusName(teacher.getCampusName());
        if (studentIds != null && !studentIds.isEmpty()) {
            NotificationAddReq notification = new NotificationAddReq();
            notification.setTitle("答辩安排已发布");
            String content = String.format(
                    "您收到新的答辩安排通知：\n场次名称：%s\n答辩日期：%s\n答辩时间：%s 至 %s\n答辩形式：%s\n地点：%s\n请同学们及时查看并做好答辩准备。",
                    req.getSessionName(),
                    req.getDefenseDate(),
                    req.getStartTime(),
                    req.getEndTime(),
                    "ONLINE".equals(req.getDefenseForm()) ? "线上" : "线下",
                    req.getLocation() != null ? req.getLocation() : "待定"
            );
            notification.setContent(content);
            notification.setNoticeType(2);
            notification.setBizType("DEFENSE");
            notification.setBizId(session.getSessionId());
            notification.setReceiverIds(studentIds);
            notificationService.add(notification, teacher.getUserId());
        }
    }

    @Override
    public Page<DefenseSessionResp> pageForAdmin(long current, long size, String campusName, String academicYear, String keyword) {
        Page<DefenseSessionResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<DefenseSessionResp> records = sessionMapper.selectSessionPage(campusName, academicYear, keyword, offset, size);
        long total = sessionMapper.countSessionPage(campusName, academicYear, keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<DefenseSessionResp> pageForTeacher(long current, long size, String academicYear, String keyword) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<DefenseSessionResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<DefenseSessionResp> records = sessionMapper.selectSessionPageForTeacher(teacher.getUserId(), academicYear, keyword, offset, size);
        long total = sessionMapper.countSessionPageForTeacher(teacher.getUserId(), academicYear, keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<DefenseSessionResp> pageForStudent(long current, long size, String academicYear) {
        SysUser student = SecurityUtils.currentUser();
        Page<DefenseSessionResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<DefenseSessionResp> records = sessionMapper.selectSessionPageForStudent(student.getUserId(), academicYear, offset, size);
        long total = sessionMapper.countSessionPageForStudent(student.getUserId(), academicYear);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<DefenseArrangeResp> arrangementPageForAdmin(long current, long size, Long sessionId, String keyword) {
        Page<DefenseArrangeResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<DefenseArrangeResp> records = sessionMapper.selectArrangementPage(sessionId, keyword, offset, size);
        long total = sessionMapper.countArrangementPage(sessionId, keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public DefenseSessionResp getSessionDetail(Long sessionId) {
        DefenseSession session = sessionMapper.selectBySessionId(sessionId);
        if (session == null) {
            throw new BizException("答辩场次不存在");
        }
        DefenseSessionResp resp = new DefenseSessionResp();
        resp.setSessionId(session.getSessionId());
        resp.setSessionName(session.getSessionName());
        resp.setDefenseDate(session.getDefenseDate() != null ? session.getDefenseDate().toString() : null);
        resp.setStartTime(session.getStartTime() != null ? session.getStartTime().toString() : null);
        resp.setEndTime(session.getEndTime() != null ? session.getEndTime().toString() : null);
        resp.setLocation(session.getLocation());
        resp.setDefenseForm(session.getDefenseForm());
        resp.setAcademicYear(session.getAcademicYear());
        resp.setFileId(session.getFileId());
        resp.setRemark(session.getRemark());
        resp.setCreateTime(session.getCreateTime() != null ? session.getCreateTime().toString() : null);
        return resp;
    }
}
