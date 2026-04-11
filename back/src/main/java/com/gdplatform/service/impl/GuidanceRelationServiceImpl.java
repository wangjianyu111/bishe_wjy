package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.*;
import com.gdplatform.entity.*;
import com.gdplatform.mapper.*;
import com.gdplatform.service.GuidanceRelationService;
import com.gdplatform.service.NotificationService;
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
public class GuidanceRelationServiceImpl implements GuidanceRelationService {

    private final GuidanceRelationMapper relationMapper;
    private final GuidanceRelationApplyMapper applyMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;
    private final DefenseGroupMapper defenseGroupMapper;
    private final DefenseGroupMemberMapper defenseGroupMemberMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final ProjectTopicMapper topicMapper;

    @Override
    public Page<GuidanceRelationResp> pageForAdmin(long current, long size, String campusName, String academicYear, String keyword) {
        Page<GuidanceRelation> page = new Page<>(current, size);
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(campusName != null && !campusName.isEmpty(), GuidanceRelation::getCampusName, campusName)
                .eq(academicYear != null && !academicYear.isEmpty(), GuidanceRelation::getAcademicYear, academicYear)
                .and(keyword != null && !keyword.isEmpty(),
                        w -> w.like(GuidanceRelation::getStudentName, keyword)
                                .or().like(GuidanceRelation::getStudentNo, keyword)
                                .or().like(GuidanceRelation::getTeacherName, keyword)
                                .or().like(GuidanceRelation::getTeacherNo, keyword))
                .eq(GuidanceRelation::getIsDeleted, 0)
                .orderByDesc(GuidanceRelation::getCreateTime);
        Page<GuidanceRelation> result = relationMapper.selectPage(page, q);
        return convertToRespPage(result);
    }

    @Override
    public Page<GuidanceRelationResp> pageForTeacher(long current, long size, String academicYear, String keyword) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<GuidanceRelation> page = new Page<>(current, size);
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(GuidanceRelation::getTeacherId, teacher.getUserId())
                .eq(academicYear != null && !academicYear.isEmpty(), GuidanceRelation::getAcademicYear, academicYear)
                .and(keyword != null && !keyword.isEmpty(),
                        w -> w.like(GuidanceRelation::getStudentName, keyword)
                                .or().like(GuidanceRelation::getStudentNo, keyword))
                .eq(GuidanceRelation::getIsDeleted, 0)
                .orderByDesc(GuidanceRelation::getCreateTime);
        Page<GuidanceRelation> result = relationMapper.selectPage(page, q);
        return convertToRespPage(result);
    }

    @Override
    public Page<GuidanceRelationResp> pageForStudent(long current, long size, String academicYear) {
        SysUser student = SecurityUtils.currentUser();
        Page<GuidanceRelation> page = new Page<>(current, size);
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(GuidanceRelation::getStudentId, student.getUserId())
                .eq(academicYear != null && !academicYear.isEmpty(), GuidanceRelation::getAcademicYear, academicYear)
                .eq(GuidanceRelation::getIsDeleted, 0)
                .orderByDesc(GuidanceRelation::getCreateTime);
        Page<GuidanceRelation> result = relationMapper.selectPage(page, q);
        return convertToRespPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(GuidanceRelationReq req) {
        SysUser currentUser = SecurityUtils.currentUser();
        SysUser student = userMapper.selectById(req.getStudentId());
        if (student == null || student.getIsDeleted() == 1) throw new BizException("学生不存在");
        if (student.getUserType() == null || student.getUserType() != 1) throw new BizException("该用户不是学生");

        SysUser teacher = userMapper.selectById(req.getTeacherId());
        if (teacher == null || teacher.getIsDeleted() == 1) throw new BizException("教师不存在");
        if (teacher.getUserType() == null || teacher.getUserType() != 2) throw new BizException("该用户不是教师");

        String academicYear = req.getAcademicYear();
        if (academicYear == null || academicYear.isEmpty()) academicYear = student.getCampusName();

        if (!student.getCampusName().equals(teacher.getCampusName())) {
            throw new BizException("学生和教师必须属于同一学校");
        }

        Long existId = relationMapper.selectRelationIdByStudentAndYear(req.getStudentId(), academicYear);
        if (existId != null) {
            throw new BizException("该学生在当前学年已有指导教师，如需变更请先解除现有关系");
        }

        GuidanceRelation relation = buildRelation(student, teacher, currentUser, academicYear);
        relationMapper.insert(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GuidanceRelationReq req) {
        if (req.getRelationId() == null) throw new BizException("关系ID不能为空");
        GuidanceRelation exist = relationMapper.selectById(req.getRelationId());
        if (exist == null || exist.getIsDeleted() == 1) throw new BizException("关系不存在");

        SysUser newTeacher = userMapper.selectById(req.getTeacherId());
        if (newTeacher == null || newTeacher.getIsDeleted() == 1) throw new BizException("新教师不存在");
        if (newTeacher.getUserType() == null || newTeacher.getUserType() != 2) throw new BizException("该用户不是教师");

        SysUser student = userMapper.selectById(exist.getStudentId());
        if (!newTeacher.getCampusName().equals(student.getCampusName())) {
            throw new BizException("新教师和原学生必须属于同一学校");
        }

        exist.setTeacherId(newTeacher.getUserId());
        exist.setTeacherName(newTeacher.getRealName());
        exist.setTeacherNo(newTeacher.getTeacherNo());
        exist.setUpdateTime(LocalDateTime.now());
        relationMapper.updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long relationId) {
        GuidanceRelation relation = relationMapper.selectById(relationId);
        if (relation == null || relation.getIsDeleted() == 1) throw new BizException("关系不存在");
        relation.setStatus("TERMINATED");
        relation.setUpdateTime(LocalDateTime.now());
        relationMapper.updateById(relation);
    }

    @Override
    public void terminateRelation(Long relationId) {
        SysUser currentUser = SecurityUtils.currentUser();
        GuidanceRelation relation = relationMapper.selectById(relationId);
        if (relation == null || relation.getIsDeleted() == 1) throw new BizException("关系不存在");

        relation.setStatus("TERMINATED");
        relation.setUpdateTime(LocalDateTime.now());
        relationMapper.updateById(relation);

        // 通知对方
        Long receiverId;
        String title;
        String content;

        if (currentUser.getUserType() == 2) {
            // 教师解除，通知学生
            receiverId = relation.getStudentId();
            title = "指导关系已解除";
            content = String.format("教师「%s」已与您解除指导关系，请联系管理员或重新申请。", currentUser.getRealName());
        } else {
            // 学生解除，通知教师
            receiverId = relation.getTeacherId();
            title = "指导关系已解除";
            content = String.format("学生「%s」（%s）已与您解除指导关系。", currentUser.getRealName(), currentUser.getStudentNo());
        }

        sendNotification(receiverId, title, content, "GUIDANCE", relationId);
    }

    @Override
    public GuidanceRelationResp getDetail(Long relationId) {
        GuidanceRelation relation = relationMapper.selectById(relationId);
        if (relation == null || relation.getIsDeleted() == 1) throw new BizException("关系不存在");
        return convertToResp(relation);
    }

    @Override
    public List<GuidanceRelationResp> listByTeacher(Long teacherId, String academicYear) {
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(GuidanceRelation::getTeacherId, teacherId)
                .eq(academicYear != null && !academicYear.isEmpty(), GuidanceRelation::getAcademicYear, academicYear)
                .eq(GuidanceRelation::getIsDeleted, 0)
                .orderByDesc(GuidanceRelation::getCreateTime);
        return relationMapper.selectList(q).stream().map(this::convertToResp).toList();
    }

    @Override
    public Long getTeacherIdByStudent(Long studentId, String academicYear) {
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(GuidanceRelation::getStudentId, studentId)
                .eq(GuidanceRelation::getAcademicYear, academicYear)
                .eq(GuidanceRelation::getIsDeleted, 0)
                .last("LIMIT 1");
        GuidanceRelation r = relationMapper.selectOne(q);
        return r != null ? r.getTeacherId() : null;
    }

    @Override
    public List<Long> getStudentIdsByTeacher(Long teacherId, String academicYear) {
        return relationMapper.selectStudentIdsByTeacherId(teacherId, academicYear, "ACTIVE");
    }

    @Override
    public boolean isStudentGuided(Long studentId, String academicYear) {
        return relationMapper.selectRelationIdByStudentAndYear(studentId, academicYear) != null;
    }

    // ====== 教师仪表盘 ======

    @Override
    public Page<GuidanceRelationPageResp> teacherStudentPage(long current, long size, String academicYear, String keyword) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<GuidanceRelation> page = new Page<>(current, size);
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(GuidanceRelation::getTeacherId, teacher.getUserId())
                .eq(academicYear != null && !academicYear.isEmpty(), GuidanceRelation::getAcademicYear, academicYear)
                .and(keyword != null && !keyword.isEmpty(),
                        w -> w.like(GuidanceRelation::getStudentName, keyword)
                                .or().like(GuidanceRelation::getStudentNo, keyword))
                .eq(GuidanceRelation::getIsDeleted, 0)
                .orderByDesc(GuidanceRelation::getCreateTime);
        Page<GuidanceRelation> result = relationMapper.selectPage(page, q);

        Page<GuidanceRelationPageResp> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(r -> {
            GuidanceRelationPageResp p = convertToPageResp(r);
            p.setDefenseGroups(enrichStudentGroups(r.getStudentId(), academicYear));
            return p;
        }).toList());
        return respPage;
    }

    @Override
    public List<DefenseGroupCard> teacherCreatedGroups(String academicYear) {
        SysUser teacher = SecurityUtils.currentUser();
        LambdaQueryWrapper<DefenseGroup> q = Wrappers.<DefenseGroup>lambdaQuery()
                .eq(DefenseGroup::getLeaderId, teacher.getUserId())
                .eq(academicYear != null && !academicYear.isEmpty(), DefenseGroup::getAcademicYear, academicYear)
                .eq(DefenseGroup::getIsDeleted, 0)
                .orderByDesc(DefenseGroup::getCreateTime);
        List<DefenseGroup> groups = defenseGroupMapper.selectList(q);
        return groups.stream().map(g -> buildGroupCard(g, true)).toList();
    }

    @Override
    public List<DefenseGroupCard> teacherJoinedGroups(String academicYear) {
        SysUser teacher = SecurityUtils.currentUser();
        List<Long> createdIds = defenseGroupMapper.selectGroupIdsByLeader(teacher.getUserId());
        List<DefenseGroup> allJoined = new ArrayList<>();

        LambdaQueryWrapper<DefenseGroupMember> mq = Wrappers.<DefenseGroupMember>lambdaQuery()
                .eq(DefenseGroupMember::getUserId, teacher.getUserId())
                .eq(DefenseGroupMember::getUserType, "TEACHER")
                .eq(DefenseGroupMember::getIsDeleted, 0);
        List<DefenseGroupMember> memberships = defenseGroupMemberMapper.selectList(mq);

        for (DefenseGroupMember m : memberships) {
            if (createdIds.contains(m.getGroupId())) continue;
            DefenseGroup g = defenseGroupMapper.selectById(m.getGroupId());
            if (g != null && g.getIsDeleted() == 0 &&
                    (academicYear == null || academicYear.isEmpty() || academicYear.equals(g.getAcademicYear()))) {
                allJoined.add(g);
            }
        }
        return allJoined.stream().map(g -> buildGroupCard(g, false)).toList();
    }

    // ====== 学生仪表盘 ======

    @Override
    public GuidanceRelationPageResp studentRelation(String academicYear) {
        SysUser student = SecurityUtils.currentUser();
        LambdaQueryWrapper<GuidanceRelation> q = Wrappers.<GuidanceRelation>lambdaQuery()
                .eq(GuidanceRelation::getStudentId, student.getUserId())
                .eq(academicYear != null && !academicYear.isEmpty(), GuidanceRelation::getAcademicYear, academicYear)
                .eq(GuidanceRelation::getIsDeleted, 0)
                .last("LIMIT 1");
        GuidanceRelation r = relationMapper.selectOne(q);
        if (r == null) return null;
        GuidanceRelationPageResp p = convertToPageResp(r);
        p.setDefenseGroups(enrichStudentGroups(r.getStudentId(), academicYear));
        return p;
    }

    @Override
    public List<DefenseGroupCard> studentJoinedGroups(String academicYear) {
        SysUser student = SecurityUtils.currentUser();
        LambdaQueryWrapper<DefenseGroupMember> mq = Wrappers.<DefenseGroupMember>lambdaQuery()
                .eq(DefenseGroupMember::getUserId, student.getUserId())
                .eq(DefenseGroupMember::getUserType, "STUDENT")
                .eq(DefenseGroupMember::getIsDeleted, 0);
        List<DefenseGroupMember> memberships = defenseGroupMemberMapper.selectList(mq);

        List<DefenseGroupCard> result = new ArrayList<>();
        for (DefenseGroupMember m : memberships) {
            DefenseGroup g = defenseGroupMapper.selectById(m.getGroupId());
            if (g != null && g.getIsDeleted() == 0 &&
                    (academicYear == null || academicYear.isEmpty() || academicYear.equals(g.getAcademicYear()))) {
                result.add(buildGroupCard(g, false));
            }
        }
        return result;
    }

    // ====== 申请流程 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendApply(GuidanceRelationApplyReq req) {
        SysUser currentUser = SecurityUtils.currentUser();

        if (currentUser.getUserType() == null) throw new BizException("用户类型未知");
        if (currentUser.getUserType() == 1 && currentUser.getUserId().equals(req.getToUserId())) {
            throw new BizException("不能给自己发送申请");
        }

        SysUser target = userMapper.selectById(req.getToUserId());
        if (target == null || target.getIsDeleted() == 1) throw new BizException("目标用户不存在");

        // 校验：教师找学生，学生找教师
        if (currentUser.getUserType() == 2) {
            if (target.getUserType() == null || target.getUserType() != 1) {
                throw new BizException("教师只能向学生发送申请");
            }
        } else if (currentUser.getUserType() == 1) {
            if (target.getUserType() == null || target.getUserType() != 2) {
                throw new BizException("学生只能向教师发送申请");
            }
        }

        if (!currentUser.getCampusName().equals(target.getCampusName())) {
            throw new BizException("双方必须属于同一学校");
        }

        String academicYear = req.getAcademicYear();
        if (academicYear == null || academicYear.isEmpty()) academicYear = currentUser.getCampusName();

        // 检查是否已有有效关系
        Long existRelation = relationMapper.selectRelationIdByStudentAndYear(
                currentUser.getUserType() == 1 ? currentUser.getUserId() : req.getToUserId(), academicYear);
        if (existRelation != null) {
            throw new BizException("该学生在当前学年已有指导关系，无法发送申请");
        }

        // 检查是否有待处理申请
        Long pendingApplyId = applyMapper.selectPendingApplyId(currentUser.getUserId(), req.getToUserId(), academicYear);
        if (pendingApplyId != null) {
            throw new BizException("您已向该用户发送过申请，请等待处理");
        }

        // 检查反向是否已有待处理
        Long reversePendingId = applyMapper.selectPendingApplyId(req.getToUserId(), currentUser.getUserId(), academicYear);
        if (reversePendingId != null) {
            throw new BizException("对方已向您发送过申请，请在申请列表中处理");
        }

        GuidanceRelationApply apply = new GuidanceRelationApply();
        apply.setFromUserId(currentUser.getUserId());
        apply.setFromUserName(currentUser.getRealName());
        apply.setFromUserType(currentUser.getUserType() == 1 ? "STUDENT" : "TEACHER");
        apply.setToUserId(req.getToUserId());
        apply.setToUserName(target.getRealName());
        apply.setToUserType(target.getUserType() == 1 ? "STUDENT" : "TEACHER");
        apply.setCampusId(currentUser.getCampusId());
        apply.setCampusName(currentUser.getCampusName());
        apply.setAcademicYear(academicYear);
        apply.setMessage(req.getMessage());
        apply.setStatus("PENDING");
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.insert(apply);

        // 通知对方
        String title = currentUser.getUserType() == 1
                ? "收到新的指导关系申请"
                : "收到指导关系申请";
        String content = currentUser.getUserType() == 1
                ? String.format("学生「%s」（%s）向您发送了指导关系申请，请前往处理。", currentUser.getRealName(), currentUser.getStudentNo())
                : String.format("教师「%s」（%s）向您发送了指导关系申请，请前往处理。", currentUser.getRealName(), currentUser.getTeacherNo());
        sendNotification(req.getToUserId(), title, content, "GUIDANCE_APPLY", apply.getApplyId());
    }

    @Override
    public Page<GuidanceRelationApplyResp> myReceivedApplies(long current, long size) {
        SysUser currentUser = SecurityUtils.currentUser();
        Page<GuidanceRelationApply> page = new Page<>(current, size);
        LambdaQueryWrapper<GuidanceRelationApply> q = Wrappers.<GuidanceRelationApply>lambdaQuery()
                .eq(GuidanceRelationApply::getToUserId, currentUser.getUserId())
                .eq(GuidanceRelationApply::getIsDeleted, 0)
                .orderByDesc(GuidanceRelationApply::getCreateTime);
        Page<GuidanceRelationApply> result = applyMapper.selectPage(page, q);

        Page<GuidanceRelationApplyResp> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(this::convertApplyToResp).toList());
        return respPage;
    }

    @Override
    public Page<GuidanceRelationApplyResp> mySentApplies(long current, long size) {
        SysUser currentUser = SecurityUtils.currentUser();
        Page<GuidanceRelationApply> page = new Page<>(current, size);
        LambdaQueryWrapper<GuidanceRelationApply> q = Wrappers.<GuidanceRelationApply>lambdaQuery()
                .eq(GuidanceRelationApply::getFromUserId, currentUser.getUserId())
                .eq(GuidanceRelationApply::getIsDeleted, 0)
                .orderByDesc(GuidanceRelationApply::getCreateTime);
        Page<GuidanceRelationApply> result = applyMapper.selectPage(page, q);

        Page<GuidanceRelationApplyResp> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(result.getRecords().stream().map(this::convertApplyToResp).toList());
        return respPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApply(GuidanceRelationHandleReq req) {
        SysUser currentUser = SecurityUtils.currentUser();
        GuidanceRelationApply apply = applyMapper.selectById(req.getApplyId());
        if (apply == null || apply.getIsDeleted() == 1) throw new BizException("申请不存在");
        if (!apply.getToUserId().equals(currentUser.getUserId())) {
            throw new BizException("只有接收方才能处理此申请");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BizException("该申请已被处理，无法重复操作");
        }

        apply.setStatus(req.getStatus());
        apply.setHandleBy(currentUser.getUserId());
        apply.setHandleByName(currentUser.getRealName());
        apply.setHandleTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        if ("APPROVED".equals(req.getStatus())) {
            SysUser teacher;
            SysUser student;
            if ("TEACHER".equals(apply.getFromUserType())) {
                teacher = userMapper.selectById(apply.getFromUserId());
                student = userMapper.selectById(apply.getToUserId());
            } else {
                student = userMapper.selectById(apply.getFromUserId());
                teacher = userMapper.selectById(apply.getToUserId());
            }

            if (teacher == null || student == null) throw new BizException("用户信息异常");

            Long existId = relationMapper.selectRelationIdByStudentAndYear(student.getUserId(), apply.getAcademicYear());
            if (existId != null) {
                throw new BizException("该学生已有指导教师，请先解除现有关系");
            }

            GuidanceRelation relation = buildRelation(student, teacher, currentUser, apply.getAcademicYear());
            relationMapper.insert(relation);

            // 通知申请方
            String title = "指导关系申请已通过";
            String content = String.format("您向%s「%s」发起的指导关系申请已通过！",
                    "STUDENT".equals(apply.getFromUserType()) ? "教师" : "学生",
                    currentUser.getRealName());
            sendNotification(apply.getFromUserId(), title, content, "GUIDANCE", relation.getRelationId());
        } else {
            // 拒绝：通知申请方
            String title = "指导关系申请被拒绝";
            String content = String.format("您发起的指导关系申请已被「%s」拒绝。", currentUser.getRealName());
            sendNotification(apply.getFromUserId(), title, content, "GUIDANCE_APPLY", apply.getApplyId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApply(Long applyId) {
        SysUser currentUser = SecurityUtils.currentUser();
        GuidanceRelationApply apply = applyMapper.selectById(applyId);
        if (apply == null || apply.getIsDeleted() == 1) throw new BizException("申请不存在");
        if (!apply.getFromUserId().equals(currentUser.getUserId())) {
            throw new BizException("只有申请方才能取消申请");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BizException("该申请已被处理，无法取消");
        }
        apply.setStatus("CANCELLED");
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(apply);
    }

    // ====== 私有工具方法 ======

    private GuidanceRelation buildRelation(SysUser student, SysUser teacher, SysUser creator, String academicYear) {
        GuidanceRelation relation = new GuidanceRelation();
        relation.setStudentId(student.getUserId());
        relation.setStudentName(student.getRealName());
        relation.setStudentNo(student.getStudentNo());
        relation.setTeacherId(teacher.getUserId());
        relation.setTeacherName(teacher.getRealName());
        relation.setTeacherNo(teacher.getTeacherNo());
        relation.setCampusId(student.getCampusId());
        relation.setCampusName(student.getCampusName());
        relation.setAcademicYear(academicYear);
        relation.setStatus("ACTIVE");
        relation.setCreateBy(creator.getUserId());
        relation.setCreateByName(creator.getRealName());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());
        return relation;
    }

    private List<DefenseGroupCard> enrichStudentGroups(Long studentId, String academicYear) {
        LambdaQueryWrapper<DefenseGroupMember> mq = Wrappers.<DefenseGroupMember>lambdaQuery()
                .eq(DefenseGroupMember::getUserId, studentId)
                .eq(DefenseGroupMember::getUserType, "STUDENT")
                .eq(DefenseGroupMember::getIsDeleted, 0);
        List<DefenseGroupMember> memberships = defenseGroupMemberMapper.selectList(mq);

        List<DefenseGroupCard> result = new ArrayList<>();
        for (DefenseGroupMember m : memberships) {
            DefenseGroup g = defenseGroupMapper.selectById(m.getGroupId());
            if (g != null && g.getIsDeleted() == 0 &&
                    (academicYear == null || academicYear.isEmpty() || academicYear.equals(g.getAcademicYear()))) {
                result.add(buildGroupCard(g, false));
            }
        }
        return result;
    }

    private DefenseGroupCard buildGroupCard(DefenseGroup g, boolean isCreator) {
        DefenseGroupCard card = new DefenseGroupCard();
        card.setGroupId(g.getGroupId());
        card.setGroupName(g.getGroupName());
        card.setLeaderId(g.getLeaderId());
        card.setLeaderName(g.getLeaderName());
        card.setCampusName(g.getCampusName());
        card.setAcademicYear(g.getAcademicYear());
        card.setStatus(g.getStatus());
        card.setIsCreator(isCreator);
        card.setCreateTime(g.getCreateTime());

        List<DefenseGroupMember> teachers = defenseGroupMemberMapper.selectTeachersByGroupId(g.getGroupId());
        List<DefenseGroupMember> students = defenseGroupMemberMapper.selectStudentsByGroupId(g.getGroupId());

        card.setTeacherCount(teachers.size());
        card.setStudentCount(students.size());

        card.setTeachers(teachers.stream().map(t -> {
            DefenseGroupCard.DefenseGroupMemberItem item = new DefenseGroupCard.DefenseGroupMemberItem();
            item.setUserId(t.getUserId());
            item.setUserName(t.getUserName());
            SysUser u = userMapper.selectById(t.getUserId());
            item.setTeacherNo(u != null ? u.getTeacherNo() : null);
            return item;
        }).toList());

        card.setStudents(students.stream().map(s -> {
            DefenseGroupCard.DefenseGroupStudentItem item = new DefenseGroupCard.DefenseGroupStudentItem();
            item.setStudentId(s.getUserId());
            item.setStudentName(s.getUserName());
            SysUser u = userMapper.selectById(s.getUserId());
            item.setStudentNo(u != null ? u.getStudentNo() : null);

            LambdaQueryWrapper<ProjectSelection> sq = Wrappers.<ProjectSelection>lambdaQuery()
                    .eq(ProjectSelection::getStudentId, s.getUserId())
                    .eq(ProjectSelection::getAcademicYear, g.getAcademicYear())
                    .eq(ProjectSelection::getIsDeleted, 0)
                    .last("LIMIT 1");
            ProjectSelection sel = selectionMapper.selectOne(sq);
            if (sel != null) {
                String topicName = null;
                if (Boolean.TRUE.equals(sel.getIsCustomTopic())) {
                    topicName = sel.getCustomTopicName();
                } else if (sel.getTopicId() != null) {
                    ProjectTopic topic = topicMapper.selectById(sel.getTopicId());
                    if (topic != null) topicName = topic.getTopicName();
                }
                item.setTopicName(topicName);
            }
            return item;
        }).toList());

        return card;
    }

    private void sendNotification(Long receiverId, String title, String content, String bizType, Long bizId) {
        try {
            NotificationAddReq notification = new NotificationAddReq();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setNoticeType(2);
            notification.setBizType(bizType);
            notification.setBizId(bizId);
            notification.setReceiverIds(List.of(receiverId));
            notificationService.add(notification, null);
        } catch (Exception ignored) {}
    }

    private GuidanceRelationPageResp convertToPageResp(GuidanceRelation r) {
        GuidanceRelationPageResp p = new GuidanceRelationPageResp();
        p.setRelationId(r.getRelationId());
        p.setStudentId(r.getStudentId());
        p.setStudentName(r.getStudentName());
        p.setStudentNo(r.getStudentNo());
        p.setTeacherId(r.getTeacherId());
        p.setTeacherName(r.getTeacherName());
        p.setTeacherNo(r.getTeacherNo());
        p.setCampusName(r.getCampusName());
        p.setAcademicYear(r.getAcademicYear());
        p.setStatus(r.getStatus());
        p.setCreateTime(r.getCreateTime());
        return p;
    }

    private Page<GuidanceRelationResp> convertToRespPage(Page<GuidanceRelation> page) {
        Page<GuidanceRelationResp> respPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        respPage.setRecords(page.getRecords().stream().map(this::convertToResp).toList());
        return respPage;
    }

    private GuidanceRelationResp convertToResp(GuidanceRelation r) {
        GuidanceRelationResp resp = new GuidanceRelationResp();
        resp.setRelationId(r.getRelationId());
        resp.setStudentId(r.getStudentId());
        resp.setStudentName(r.getStudentName());
        resp.setStudentNo(r.getStudentNo());
        resp.setTeacherId(r.getTeacherId());
        resp.setTeacherName(r.getTeacherName());
        resp.setTeacherNo(r.getTeacherNo());
        resp.setCampusId(r.getCampusId());
        resp.setCampusName(r.getCampusName());
        resp.setAcademicYear(r.getAcademicYear());
        resp.setStatus(r.getStatus());
        resp.setCreateBy(r.getCreateBy());
        resp.setCreateByName(r.getCreateByName());
        resp.setCreateTime(r.getCreateTime());
        resp.setUpdateTime(r.getUpdateTime());
        return resp;
    }

    private GuidanceRelationApplyResp convertApplyToResp(GuidanceRelationApply a) {
        GuidanceRelationApplyResp resp = new GuidanceRelationApplyResp();
        resp.setApplyId(a.getApplyId());
        resp.setFromUserId(a.getFromUserId());
        resp.setFromUserName(a.getFromUserName());
        resp.setFromUserType(a.getFromUserType());
        resp.setToUserId(a.getToUserId());
        resp.setToUserName(a.getToUserName());
        resp.setToUserType(a.getToUserType());
        resp.setCampusName(a.getCampusName());
        resp.setAcademicYear(a.getAcademicYear());
        resp.setMessage(a.getMessage());
        resp.setStatus(a.getStatus());
        resp.setHandleByName(a.getHandleByName());
        resp.setHandleTime(a.getHandleTime());
        resp.setCreateTime(a.getCreateTime());
        return resp;
    }
}
