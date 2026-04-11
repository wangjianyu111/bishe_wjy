package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.DocFile;
import com.gdplatform.entity.DocVersion;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.mapper.DocVersionMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.DocVersionService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocVersionServiceImpl implements DocVersionService {

    private final DocVersionMapper versionMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final DocFileMapper docFileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(VersionSubmitReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可提交文档版本");
        }
        if (req.getSelectionId() == null) {
            throw new BizException("请先选择选题");
        }
        if (req.getStageName() == null || req.getStageName().isBlank()) {
            throw new BizException("请填写毕设阶段名称");
        }

        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("只能提交自己的文档版本");
        }
        if (!"APPROVED".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有审批通过的选题才能提交文档");
        }

        DocVersion latest = versionMapper.selectLatestByStage(sel.getSelectionId(), req.getStageName().trim());
        int nextVersion = 1;
        if (latest != null) {
            nextVersion = latest.getVersionNo() + 1;
        }

        DocVersion record = new DocVersion();
        record.setSelectionId(sel.getSelectionId());
        record.setStudentId(user.getUserId());
        record.setStageName(req.getStageName().trim());
        record.setVersionNo(nextVersion);
        record.setFileId(req.getFileId());
        record.setRemark(req.getRemark());
        record.setStatus("SUBMITTED");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        versionMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByStudent(Long versionId) {
        SysUser user = SecurityUtils.currentUser();
        DocVersion version = versionMapper.selectByVersionId(versionId);
        if (version == null) {
            throw new BizException("文档版本记录不存在");
        }
        if (!version.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权删除他人的文档");
        }
        if (!"SUBMITTED".equalsIgnoreCase(version.getStatus())) {
            throw new BizException("仅可删除待提交状态的文档");
        }
        if (version.getFileId() != null) {
            docFileMapper.deleteById(version.getFileId());
        }
        versionMapper.deleteByVersionId(versionId);
    }

    @Override
    public Page<VersionResp> pageForAdmin(VersionPageReq req, long current, long size) {
        Page<VersionResp> page = new Page<>(current, size);
        List<VersionResp> records = versionMapper.selectVersionPage(req, (current - 1) * size, size);
        long total = versionMapper.countVersionPage(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<VersionResp> pageForTeacher(VersionPageReq req, long current, long size) {
        SysUser teacher = SecurityUtils.currentUser();
        req.setTeacherId(teacher.getUserId());
        Page<VersionResp> page = new Page<>(current, size);
        List<VersionResp> records = versionMapper.selectVersionPageForTeacher(req, (current - 1) * size, size);
        long total = versionMapper.countVersionPageForTeacher(req);
        records.forEach(this::enrichStatusLabel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<VersionResp> listByStudent(Long selectionId) {
        SysUser user = SecurityUtils.currentUser();
        VersionPageReq req = new VersionPageReq();
        req.setStudentId(user.getUserId());
        if (selectionId != null) {
            req.setSelectionId(selectionId);
        }
        List<VersionResp> list = versionMapper.selectVersionPage(req, 0L, 1000L);
        list.forEach(this::enrichStatusLabel);
        return list;
    }

    @Override
    public VersionResp getDetail(Long versionId) {
        DocVersion version = versionMapper.selectByVersionId(versionId);
        if (version == null) {
            throw new BizException("文档版本记录不存在");
        }
        VersionPageReq req = new VersionPageReq();
        req.setStudentId(version.getStudentId());
        List<VersionResp> list = versionMapper.selectVersionPage(req, 0L, 1L);
        VersionResp resp = !list.isEmpty() ? list.get(0) : null;
        if (resp != null) {
            enrichStatusLabel(resp);
        }
        return resp;
    }

    private void enrichStatusLabel(VersionResp resp) {
        // 无需格式化，VersionResp 中的时间字段已经是字符串类型
    }
}
