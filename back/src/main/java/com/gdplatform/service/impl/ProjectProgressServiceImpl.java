package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectProgress;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.ProjectProgressMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.ProjectProgressService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectProgressServiceImpl implements ProjectProgressService {

    private final ProjectProgressMapper progressMapper;
    private final ProjectSelectionMapper selectionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProgress(ProgressAddReq req) {
        SysUser user = SecurityUtils.currentUser();

        if (user.getUserType() == 1) {
            ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
            if (sel == null) {
                throw new BizException("选题记录不存在");
            }
            if (!sel.getStudentId().equals(user.getUserId())) {
                throw new BizException("只能填写自己的选题进度");
            }
            if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
                throw new BizException("只有审批通过的选题才能填写进度");
            }
        }

        ProjectProgress progress = new ProjectProgress();
        progress.setSelectionId(req.getSelectionId());
        progress.setPhase(req.getPhase());
        progress.setContent(req.getContent());
        progress.setPlanDate(req.getPlanDate());
        progress.setPercentDone(0);
        progress.setSubmitTime(LocalDateTime.now());
        progress.setIsDeleted(0);
        progressMapper.insert(progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(ProgressUpdateReq req) {
        SysUser user = SecurityUtils.currentUser();
        ProjectProgress progress = progressMapper.selectById(req.getProgressId());
        if (progress == null) {
            throw new BizException("进度记录不存在");
        }

        if (user.getUserType() == 1 && !user.getUserId().equals(
                selectionMapper.selectById(progress.getSelectionId()).getStudentId())) {
            throw new BizException("无权修改他人的进度记录");
        }

        progress.setPhase(req.getPhase());
        progress.setContent(req.getContent());
        if (req.getPlanDate() != null) {
            progress.setPlanDate(req.getPlanDate());
        }
        if (req.getActualDate() != null) {
            progress.setActualDate(req.getActualDate());
        }
        if (req.getPercentDone() != null) {
            progress.setPercentDone(req.getPercentDone());
        }
        progressMapper.updateById(progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProgress(Long progressId) {
        SysUser user = SecurityUtils.currentUser();
        ProjectProgress progress = progressMapper.selectById(progressId);
        if (progress == null) {
            throw new BizException("进度记录不存在");
        }

        ProjectSelection sel = selectionMapper.selectById(progress.getSelectionId());
        if (sel != null && sel.getStudentId().equals(user.getUserId())) {
            progressMapper.deleteById(progressId);
        } else if (user.getUserType() == 3) {
            progressMapper.deleteById(progressId);
        } else {
            throw new BizException("无权删除此进度记录");
        }
    }

    @Override
    public List<ProgressResp> getProgressListBySelection(Long selectionId) {
        ProgressPageReq req = new ProgressPageReq();
        req.setSelectionId(selectionId);
        return progressMapper.selectProgressPage(req, 0, 100);
    }

    @Override
    public Page<ProgressResp> pageProgressForAdmin(ProgressPageReq req, long current, long size) {
        Page<ProgressResp> page = new Page<>(current, size);
        List<ProgressResp> records = progressMapper.selectProgressPage(req, (current - 1) * size, size);
        long total = progressMapper.countProgressPage(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ProgressResp> pageProgressForTeacher(ProgressPageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<ProgressResp> page = new Page<>(current, size);
        List<ProgressResp> records = progressMapper.selectProgressPageForTeacher(req, (current - 1) * size, size);
        long total = progressMapper.countProgressPageForTeacher(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ProgressResp> pageProgressForStudent(ProgressPageReq req, long current, long size) {
        SysUser student = SecurityUtils.currentUser();
        req.setStudentId(student.getUserId());
        Page<ProgressResp> page = new Page<>(current, size);
        List<ProgressResp> records = progressMapper.selectProgressPageForStudent(req, (current - 1) * size, size);
        long total = progressMapper.countProgressPageForStudent(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public ProgressResp getProgressDetail(Long progressId) {
        return null;
    }
}