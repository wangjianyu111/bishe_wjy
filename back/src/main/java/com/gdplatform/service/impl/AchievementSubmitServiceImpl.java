package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.AchievementSubmit;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.AchievementSubmitMapper;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.AchievementSubmitService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementSubmitServiceImpl implements AchievementSubmitService {

    private final AchievementSubmitMapper submitMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final DocFileMapper docFileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(AchievementSubmitReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可提交成果");
        }
        if (req.getSelectionId() == null) {
            throw new BizException("请先选择选题");
        }
        if (req.getFileId() == null) {
            throw new BizException("请上传成果压缩包");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("只能提交自己的成果");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有审批通过的选题才能提交成果");
        }

        AchievementSubmit existing = submitMapper.selectBySelectionId(req.getSelectionId());
        if (existing != null) {
            if (existing.getFileId() != null) {
                docFileMapper.deleteById(existing.getFileId());
            }
            existing.setFileId(req.getFileId());
            existing.setRemark(req.getRemark());
            existing.setUpdateTime(LocalDateTime.now());
            submitMapper.updateById(existing);
            return existing.getSubmitId();
        }

        AchievementSubmit record = new AchievementSubmit();
        record.setSelectionId(sel.getSelectionId());
        record.setStudentId(user.getUserId());
        record.setFileId(req.getFileId());
        record.setRemark(req.getRemark());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        submitMapper.insert(record);
        return record.getSubmitId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByStudent(Long submitId) {
        SysUser user = SecurityUtils.currentUser();
        AchievementSubmit submit = submitMapper.selectBySubmitId(submitId);
        if (submit == null) {
            throw new BizException("成果提交记录不存在");
        }
        if (!submit.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权删除他人的成果");
        }
        if (submit.getFileId() != null) {
            docFileMapper.deleteById(submit.getFileId());
        }
        submitMapper.deleteBySubmitId(submitId);
    }

    @Override
    public Page<AchievementResp> pageForAdmin(AchievementPageReq req, long current, long size) {
        Page<AchievementResp> page = new Page<>(current, size);
        List<AchievementResp> records = submitMapper.selectAchievementPage(req, (current - 1) * size, size);
        long total = submitMapper.countAchievementPage(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<AchievementResp> pageForTeacher(AchievementPageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<AchievementResp> page = new Page<>(current, size);
        List<AchievementResp> records = submitMapper.selectAchievementPageForTeacher(req, (current - 1) * size, size);
        long total = submitMapper.countAchievementPageForTeacher(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<AchievementResp> listByStudent(Long selectionId) {
        SysUser user = SecurityUtils.currentUser();
        AchievementPageReq req = new AchievementPageReq();
        req.setStudentId(user.getUserId());
        if (selectionId != null) {
            req.setSelectionId(selectionId);
        }
        return submitMapper.selectAchievementPage(req, 0L, 1000L);
    }

    @Override
    public AchievementResp getDetail(Long submitId) {
        AchievementSubmit submit = submitMapper.selectBySubmitId(submitId);
        if (submit == null) {
            throw new BizException("成果提交记录不存在");
        }
        AchievementPageReq req = new AchievementPageReq();
        req.setStudentId(submit.getStudentId());
        List<AchievementResp> list = submitMapper.selectAchievementPage(req, 0L, 1L);
        return !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public AchievementResp getMySubmission(Long selectionId) {
        AchievementSubmit submit = submitMapper.selectBySelectionId(selectionId);
        if (submit == null) {
            return null;
        }
        return getDetail(submit.getSubmitId());
    }
}
