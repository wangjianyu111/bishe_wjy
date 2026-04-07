package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.ProjectTopicMapper;
import com.gdplatform.mapper.CampusMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.ProjectSelectionService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectSelectionServiceImpl implements ProjectSelectionService {

    private final ProjectSelectionMapper selectionMapper;
    private final ProjectTopicMapper topicMapper;
    private final SysUserMapper userMapper;
    private final CampusMapper campusMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(SelectionApplyReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BizException("仅学生可申请选题");
        }

        // 检查是否已申请
        long exists = selectionMapper.selectCount(
                Wrappers.<ProjectSelection>lambdaQuery()
                        .eq(ProjectSelection::getStudentId, user.getUserId())
                        .eq(ProjectSelection::getAcademicYear, req.getAcademicYear()));
        if (exists > 0) {
            throw new BizException("本学年已提交过选题申请");
        }

        ProjectSelection sel = new ProjectSelection();
        sel.setStudentId(user.getUserId());
        sel.setCampusName(req.getCampusName());
        sel.setTeacherId(req.getTeacherId());
        sel.setAcademicYear(req.getAcademicYear());
        sel.setApplyReason(req.getApplyReason());
        sel.setStatus("PENDING");

        if (req.getTopicId() != null) {
            // 来自题目库
            sel.setIsCustomTopic(false);
            sel.setTopicId(req.getTopicId());

            ProjectTopic topic = topicMapper.selectById(req.getTopicId());
            if (topic == null) {
                throw new BizException("课题不存在");
            }
            if (!"OPEN".equalsIgnoreCase(topic.getStatus())) {
                throw new BizException("课题未开放选题");
            }
            if (topic.getCurrentCount() != null && topic.getMaxStudents() != null
                    && topic.getCurrentCount() >= topic.getMaxStudents()) {
                throw new BizException("课题名额已满");
            }
        } else if (req.getCustomTopicName() != null && !req.getCustomTopicName().isBlank()) {
            // 自填选题
            sel.setIsCustomTopic(true);
            sel.setCustomTopicName(req.getCustomTopicName());
            sel.setCustomTopicDescription(req.getCustomTopicDescription());
        } else {
            throw new BizException("请选择题目库课题或填写自拟课题名称");
        }

        selectionMapper.insert(sel);
    }

    @Override
    public Page<SelectionResp> pageSelections(SelectionPageReq req, long current, long size) {
        Page<SelectionResp> page = new Page<>(current, size);
        List<SelectionResp> records = selectionMapper.selectRespPage(req, (current - 1) * size, size);
        long total = selectionMapper.countRespPage(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public SelectionResp getMySelection() {
        SysUser user = SecurityUtils.currentUser();
        LambdaQueryWrapper<ProjectSelection> q = Wrappers.<ProjectSelection>lambdaQuery()
                .eq(ProjectSelection::getStudentId, user.getUserId())
                .orderByDesc(ProjectSelection::getCreateTime)
                .last("LIMIT 1");
        ProjectSelection sel = selectionMapper.selectOne(q);
        if (sel == null) {
            return null;
        }
        return buildResp(sel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(SelectionActionReq req) {
        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("申请记录不存在");
        }
        if (!"PENDING".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("该申请已被处理，无需重复审批");
        }

        sel.setStatus("APPROVED");
        selectionMapper.updateById(sel);

        // 如果是题目库选题，人数+1
        if (sel.getTopicId() != null) {
            topicMapper.addCurrentCount(sel.getTopicId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(SelectionActionReq req) {
        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("申请记录不存在");
        }
        if (!"PENDING".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("该申请已被处理，无需重复审批");
        }
        sel.setStatus("REJECTED");
        sel.setRejectReason(req.getComment());
        selectionMapper.updateById(sel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long selectionId) {
        SysUser user = SecurityUtils.currentUser();
        ProjectSelection sel = selectionMapper.selectById(selectionId);
        if (sel == null) {
            throw new BizException("申请记录不存在");
        }
        if (!sel.getStudentId().equals(user.getUserId())) {
            throw new BizException("无权撤回他人申请");
        }
        if (!"PENDING".equalsIgnoreCase(sel.getStatus())) {
            throw new BizException("只有待审状态的申请才能撤回");
        }
        sel.setStatus("WITHDRAWN");
        selectionMapper.updateById(sel);
    }

    @Override
    public List<TeacherResp> listTeachersByCampus(Long campusId) {
        return userMapper.selectTeachersByCampus(campusId);
    }

    @Override
    public List<TeacherResp> listTeachersByCampusName(String campusName) {
        return userMapper.selectTeachersByCampusName(campusName);
    }

    @Override
    public List<TopicBankItemResp> listTopicBankByCampus(Long campusId, Long teacherId, String academicYear) {
        return topicMapper.selectTopicBankByCampus(campusId, teacherId, academicYear);
    }

    @Override
    public List<CampusResp> listCampuses() {
        return campusMapper.selectAllCampuses();
    }

    private SelectionResp buildResp(ProjectSelection sel) {
        SelectionResp resp = new SelectionResp();
        resp.setSelectionId(sel.getSelectionId());
        resp.setStudentId(sel.getStudentId());
        resp.setTopicId(sel.getTopicId());
        resp.setCampusName(sel.getCampusName());
        resp.setTeacherId(sel.getTeacherId());
        resp.setAcademicYear(sel.getAcademicYear());
        resp.setStatus(sel.getStatus());
        resp.setApplyReason(sel.getApplyReason());
        resp.setRejectReason(sel.getRejectReason());
        resp.setIsCustomTopic(sel.getIsCustomTopic());
        resp.setCustomTopicName(sel.getCustomTopicName());
        resp.setCustomTopicDescription(sel.getCustomTopicDescription());
        resp.setCreateTime(sel.getCreateTime());
        resp.setUpdateTime(sel.getUpdateTime());
        resp.setStatusLabel(statusLabel(sel.getStatus()));

        // 填充学生信息
        SysUser student = userMapper.selectById(sel.getStudentId());
        if (student != null) {
            resp.setStudentName(student.getRealName());
            resp.setStudentNo(student.getStudentNo());
        }

        // 填充课题信息
        if (sel.getTopicId() != null) {
            ProjectTopic topic = topicMapper.selectById(sel.getTopicId());
            if (topic != null) {
                resp.setTopicName(topic.getTopicName());
                resp.setTopicDescription(topic.getDescription());
            }
        }

        // 填充教师信息
        if (sel.getTeacherId() != null) {
            SysUser teacher = userMapper.selectById(sel.getTeacherId());
            if (teacher != null) {
                resp.setTeacherName(teacher.getRealName());
                resp.setTeacherNo(teacher.getTeacherNo());
            }
        }

        // 填充学校信息
        resp.setCampusName(sel.getCampusName());

        return resp;
    }

    private String statusLabel(String status) {
        if (status == null) return "—";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "待审批";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            case "WITHDRAWN" -> "已撤回";
            default -> status;
        };
    }
}
