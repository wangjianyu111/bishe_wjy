package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.DefenseGroupReq;
import com.gdplatform.dto.DefenseGroupResp;
import com.gdplatform.entity.DefenseGroup;
import com.gdplatform.entity.DefenseGroupMember;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.DefenseGroupMapper;
import com.gdplatform.mapper.DefenseGroupMemberMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.ProjectTopicMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.DefenseGroupService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefenseGroupServiceImpl implements DefenseGroupService {

    private final DefenseGroupMapper groupMapper;
    private final DefenseGroupMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final ProjectTopicMapper topicMapper;

    @Override
    public Page<DefenseGroupResp> pageForTeacher(long current, long size, String academicYear, String keyword) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<DefenseGroup> page = new Page<>(current, size);

        LambdaQueryWrapper<DefenseGroup> q = Wrappers.<DefenseGroup>lambdaQuery()
                .eq(DefenseGroup::getCampusId, teacher.getCampusId())
                .eq(academicYear != null && !academicYear.isEmpty(), DefenseGroup::getAcademicYear, academicYear)
                .and(keyword != null && !keyword.isEmpty(),
                        w -> w.like(DefenseGroup::getGroupName, keyword)
                                .or().like(DefenseGroup::getLeaderName, keyword))
                .eq(DefenseGroup::getIsDeleted, 0)
                .orderByDesc(DefenseGroup::getCreateTime);

        Page<DefenseGroup> result = groupMapper.selectPage(page, q);
        Page<DefenseGroupResp> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(this::enrichGroup).toList());
        return respPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefenseGroupResp create(DefenseGroupReq req) {
        SysUser currentUser = SecurityUtils.currentUser();
        if (currentUser.getUserType() != 2) {
            throw new BizException("只有教师才能创建答辩小组");
        }

        if (req.getTeacherIds() == null || req.getTeacherIds().isEmpty()) {
            throw new BizException("请至少选择一位答辩教师");
        }

        if (!req.getTeacherIds().contains(currentUser.getUserId())) {
            throw new BizException("创建者必须是答辩小组成员");
        }

        for (Long teacherId : req.getTeacherIds()) {
            SysUser teacher = userMapper.selectById(teacherId);
            if (teacher == null || teacher.getIsDeleted() == 1) {
                throw new BizException("教师ID[" + teacherId + "]不存在");
            }
            if (teacher.getUserType() == null || teacher.getUserType() != 2) {
                throw new BizException("用户[" + teacher.getRealName() + "]不是教师");
            }
            if (!teacher.getCampusName().equals(currentUser.getCampusName())) {
                throw new BizException("教师[" + teacher.getRealName() + "]不属于同一学校");
            }
        }

        if (req.getStudentIds() != null && !req.getStudentIds().isEmpty()) {
            for (Long studentId : req.getStudentIds()) {
                SysUser student = userMapper.selectById(studentId);
                if (student == null || student.getIsDeleted() == 1) {
                    throw new BizException("学生ID[" + studentId + "]不存在");
                }
                if (student.getUserType() == null || student.getUserType() != 1) {
                    throw new BizException("用户[" + student.getRealName() + "]不是学生");
                }
                if (!student.getCampusName().equals(currentUser.getCampusName())) {
                    throw new BizException("学生[" + student.getRealName() + "]不属于同一学校");
                }
            }
        }

        DefenseGroup group = new DefenseGroup();
        group.setGroupName(req.getGroupName());
        group.setLeaderId(currentUser.getUserId());
        group.setLeaderName(currentUser.getRealName());
        group.setCampusId(currentUser.getCampusId());
        group.setCampusName(currentUser.getCampusName());
        group.setAcademicYear(req.getAcademicYear() != null ? req.getAcademicYear() : currentUser.getCampusName());
        group.setStatus("ACTIVE");
        group.setCreateBy(currentUser.getUserId());
        group.setCreateByName(currentUser.getRealName());
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.insert(group);

        for (Long teacherId : req.getTeacherIds()) {
            SysUser teacher = userMapper.selectById(teacherId);
            DefenseGroupMember member = new DefenseGroupMember();
            member.setGroupId(group.getGroupId());
            member.setUserId(teacherId);
            member.setUserName(teacher.getRealName());
            member.setUserType("TEACHER");
            member.setCampusId(teacher.getCampusId());
            member.setCampusName(teacher.getCampusName());
            member.setCreateBy(currentUser.getUserId());
            member.setCreateTime(LocalDateTime.now());
            memberMapper.insert(member);
        }

        if (req.getStudentIds() != null && !req.getStudentIds().isEmpty()) {
            for (Long studentId : req.getStudentIds()) {
                SysUser student = userMapper.selectById(studentId);
                DefenseGroupMember member = new DefenseGroupMember();
                member.setGroupId(group.getGroupId());
                member.setUserId(studentId);
                member.setUserName(student.getRealName());
                member.setUserType("STUDENT");
                member.setCampusId(student.getCampusId());
                member.setCampusName(student.getCampusName());
                member.setCreateBy(currentUser.getUserId());
                member.setCreateTime(LocalDateTime.now());
                memberMapper.insert(member);
            }
        }

        return enrichGroup(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DefenseGroupReq req) {
        if (req.getGroupId() == null) {
            throw new BizException("小组ID不能为空");
        }
        DefenseGroup group = groupMapper.selectById(req.getGroupId());
        if (group == null || group.getIsDeleted() == 1) {
            throw new BizException("小组不存在");
        }

        SysUser currentUser = SecurityUtils.currentUser();

        if (!group.getLeaderId().equals(currentUser.getUserId())) {
            throw new BizException("只有组长才能修改小组信息");
        }

        group.setGroupName(req.getGroupName());
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);

        if (req.getTeacherIds() != null) {
            memberMapper.deleteByGroupId(req.getGroupId());
            for (Long teacherId : req.getTeacherIds()) {
                SysUser teacher = userMapper.selectById(teacherId);
                DefenseGroupMember member = new DefenseGroupMember();
                member.setGroupId(group.getGroupId());
                member.setUserId(teacherId);
                member.setUserName(teacher.getRealName());
                member.setUserType("TEACHER");
                member.setCampusId(teacher.getCampusId());
                member.setCampusName(teacher.getCampusName());
                member.setCreateBy(currentUser.getUserId());
                member.setCreateTime(LocalDateTime.now());
                memberMapper.insert(member);
            }
        }

        if (req.getStudentIds() != null) {
            List<DefenseGroupMember> existingStudents = memberMapper.selectStudentsByGroupId(req.getGroupId());
            for (DefenseGroupMember existing : existingStudents) {
                DefenseGroupMember m = new DefenseGroupMember();
                m.setMemberId(existing.getMemberId());
                m.setIsDeleted(1);
                memberMapper.updateById(m);
            }
            for (Long studentId : req.getStudentIds()) {
                SysUser student = userMapper.selectById(studentId);
                DefenseGroupMember member = new DefenseGroupMember();
                member.setGroupId(group.getGroupId());
                member.setUserId(studentId);
                member.setUserName(student.getRealName());
                member.setUserType("STUDENT");
                member.setCampusId(student.getCampusId());
                member.setCampusName(student.getCampusName());
                member.setCreateBy(currentUser.getUserId());
                member.setCreateTime(LocalDateTime.now());
                memberMapper.insert(member);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long groupId) {
        DefenseGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BizException("小组不存在");
        }
        SysUser currentUser = SecurityUtils.currentUser();
        if (!group.getLeaderId().equals(currentUser.getUserId())) {
            throw new BizException("只有组长才能删除小组");
        }
        group.setIsDeleted(1);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        memberMapper.deleteByGroupId(groupId);
    }

    @Override
    public DefenseGroupResp getDetail(Long groupId) {
        DefenseGroup group = groupMapper.selectById(groupId);
        if (group == null || group.getIsDeleted() == 1) {
            throw new BizException("小组不存在");
        }
        return enrichGroup(group);
    }

    @Override
    public List<DefenseGroupResp> listByTeacher(Long teacherId, String academicYear) {
        if (teacherId == null) {
            SysUser currentUser = SecurityUtils.currentUser();
            teacherId = currentUser.getUserId();
        }
        List<Long> groupIds = groupMapper.selectGroupIdsByTeacherId(teacherId, academicYear);
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<DefenseGroup> groups = groupMapper.selectList(
                Wrappers.<DefenseGroup>lambdaQuery()
                        .in(DefenseGroup::getGroupId, groupIds)
                        .eq(DefenseGroup::getIsDeleted, 0)
                        .orderByDesc(DefenseGroup::getCreateTime));
        return groups.stream().map(this::enrichGroup).collect(Collectors.toList());
    }

    @Override
    public List<Long> getGroupIdsByTeacherId(Long teacherId, String academicYear) {
        return groupMapper.selectGroupIdsByTeacherId(teacherId, academicYear);
    }

    @Override
    public boolean isTeacherInGroup(Long teacherId, Long groupId) {
        LambdaQueryWrapper<DefenseGroupMember> q = Wrappers.<DefenseGroupMember>lambdaQuery()
                .eq(DefenseGroupMember::getGroupId, groupId)
                .eq(DefenseGroupMember::getUserId, teacherId)
                .eq(DefenseGroupMember::getUserType, "TEACHER")
                .eq(DefenseGroupMember::getIsDeleted, 0)
                .last("LIMIT 1");
        return memberMapper.selectOne(q) != null;
    }

    @Override
    public List<Long> getTeacherIdsInGroup(Long groupId) {
        return groupMapper.selectTeacherIdsByGroupId(groupId);
    }

    @Override
    public List<Long> getStudentIdsInGroup(Long groupId, Long studentId) {
        return groupMapper.selectStudentIdsByGroupIdAndStudent(groupId, studentId);
    }

    @Override
    public List<Long> getStudentIdsInGroupByTeacher(Long teacherId, String academicYear) {
        List<Long> groupIds = groupMapper.selectGroupIdsByTeacherId(teacherId, academicYear);
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> studentIds = new ArrayList<>();
        for (Long groupId : groupIds) {
            List<Long> ids = getStudentIdsInGroup(groupId, null);
            studentIds.addAll(ids);
        }
        return studentIds.stream().distinct().collect(Collectors.toList());
    }

    private DefenseGroupResp enrichGroup(DefenseGroup group) {
        DefenseGroupResp resp = new DefenseGroupResp();
        resp.setGroupId(group.getGroupId());
        resp.setGroupName(group.getGroupName());
        resp.setLeaderId(group.getLeaderId());
        resp.setLeaderName(group.getLeaderName());
        resp.setCampusId(group.getCampusId());
        resp.setCampusName(group.getCampusName());
        resp.setAcademicYear(group.getAcademicYear());
        resp.setStatus(group.getStatus());
        resp.setCreateBy(group.getCreateBy());
        resp.setCreateByName(group.getCreateByName());
        resp.setCreateTime(group.getCreateTime());

        List<DefenseGroupMember> teachers = memberMapper.selectTeachersByGroupId(group.getGroupId());
        List<DefenseGroupMember> students = memberMapper.selectStudentsByGroupId(group.getGroupId());

        resp.setTeachers(teachers.stream().map(t -> {
            DefenseGroupResp.DefenseGroupMemberResp m = new DefenseGroupResp.DefenseGroupMemberResp();
            m.setMemberId(t.getMemberId());
            m.setUserId(t.getUserId());
            m.setUserName(t.getUserName());
            SysUser u = userMapper.selectById(t.getUserId());
            m.setTeacherNo(u != null ? u.getTeacherNo() : null);
            m.setUserType(t.getUserType());
            return m;
        }).collect(Collectors.toList()));

        resp.setStudents(students.stream().map(s -> {
            DefenseGroupResp.DefenseGroupStudentResp st = new DefenseGroupResp.DefenseGroupStudentResp();
            st.setStudentId(s.getUserId());
            st.setStudentName(s.getUserName());
            SysUser u = userMapper.selectById(s.getUserId());
            st.setStudentNo(u != null ? u.getStudentNo() : null);

            LambdaQueryWrapper<ProjectSelection> sq = Wrappers.<ProjectSelection>lambdaQuery()
                    .eq(ProjectSelection::getStudentId, s.getUserId())
                    .eq(ProjectSelection::getAcademicYear, group.getAcademicYear())
                    .eq(ProjectSelection::getIsDeleted, 0)
                    .last("LIMIT 1");
            ProjectSelection sel = selectionMapper.selectOne(sq);
            if (sel != null) {
                st.setSelectionId(sel.getSelectionId());
                String topicName = null;
                if (sel.getIsCustomTopic() != null && sel.getIsCustomTopic()) {
                    topicName = sel.getCustomTopicName();
                } else if (sel.getTopicId() != null) {
                    ProjectTopic topic = topicMapper.selectById(sel.getTopicId());
                    if (topic != null) topicName = topic.getTopicName();
                }
                st.setTopicName(topicName);
            }
            return st;
        }).collect(Collectors.toList()));

        return resp;
    }
}
