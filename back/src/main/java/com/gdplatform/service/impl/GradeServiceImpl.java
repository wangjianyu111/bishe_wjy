package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.GradeInputReq;
import com.gdplatform.dto.GradeResp;
import com.gdplatform.dto.NotificationAddReq;
import com.gdplatform.entity.GradeEvaluation;
import com.gdplatform.entity.GradeSummary;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.GradeEvaluationMapper;
import com.gdplatform.mapper.GradeSummaryMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.GradeService;
import com.gdplatform.service.NotificationService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeEvaluationMapper gradeMapper;
    private final GradeSummaryMapper summaryMapper;
    private final ProjectSelectionMapper selectionMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    // 成绩等级阈值
    private static final BigDecimal LEVEL_EXCELLENT = new BigDecimal("90");
    private static final BigDecimal LEVEL_GOOD = new BigDecimal("80");
    private static final BigDecimal LEVEL_MEDIUM = new BigDecimal("70");
    private static final BigDecimal LEVEL_PASS = new BigDecimal("60");

    // 权重：平时30% 论文40% 答辩30%
    private static final BigDecimal WEIGHT_REGULAR = new BigDecimal("0.30");
    private static final BigDecimal WEIGHT_THESIS = new BigDecimal("0.40");
    private static final BigDecimal WEIGHT_DEFENSE = new BigDecimal("0.30");

    @Override
    public Page<GradeResp> adminPage(long current, long size, String campusName, String academicYear,
                                     String keyword, String gradeLevel) {
        Page<GradeResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<GradeResp> records = gradeMapper.selectAdminPage(
                campusName, academicYear, keyword, gradeLevel, offset, size);
        for (GradeResp r : records) {
            enrichDetailList(r);
        }
        long total = gradeMapper.countAdminPage(campusName, academicYear, keyword, gradeLevel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<GradeResp> teacherPage(long current, long size, String campusName, String academicYear,
                                       String keyword, String gradeLevel) {
        SysUser teacher = SecurityUtils.currentUser();
        Page<GradeResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<GradeResp> records = gradeMapper.selectTeacherPage(
                campusName, academicYear, keyword, gradeLevel, offset, size);
        for (GradeResp r : records) {
            enrichDetailList(r);
        }
        long total = gradeMapper.countTeacherPage(campusName, academicYear, keyword, gradeLevel);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<GradeResp> studentPage(long current, long size) {
        SysUser student = SecurityUtils.currentUser();
        Page<GradeResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<GradeResp> records = gradeMapper.selectStudentPage(
                student.getUserId(), offset, size);
        for (GradeResp r : records) {
            enrichDetailList(r);
        }
        long total = gradeMapper.countStudentPage(student.getUserId());
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long inputScore(GradeInputReq req) {
        SysUser teacher = SecurityUtils.currentUser();
        if (teacher.getUserType() == null || teacher.getUserType() != 2) {
            throw new BizException("只有教师才能录入成绩");
        }
        if (req.getSelectionId() == null || req.getStudentId() == null) {
            throw new BizException("选题和学生不能为空");
        }

        // 校验选题存在
        ProjectSelection sel = selectionMapper.selectById(req.getSelectionId());
        if (sel == null) {
            throw new BizException("选题不存在");
        }

        // 校验选题所属学生是否匹配
        if (!sel.getStudentId().equals(req.getStudentId())) {
            throw new BizException("选题与学生不匹配");
        }

        // 校验学校匹配（教师只能给本校学生打分）
        if (teacher.getCampusName() == null || !teacher.getCampusName().equals(sel.getCampusName())) {
            throw new BizException("您只能给本校的学生打分");
        }

        // 检查是否已锁定
        GradeSummary summary = summaryMapper.selectOne(
                Wrappers.<GradeSummary>query().eq("selection_id", req.getSelectionId()).eq("is_deleted", 0));
        if (summary != null && summary.getIsLocked() != null && summary.getIsLocked() == 1) {
            throw new BizException("该学生成绩已锁定，无法修改");
        }

        // 计算总分
        BigDecimal totalScore = calcTotalScore(req.getRegularScore(), req.getThesisScore(), req.getDefenseScore());

        // 查询该教师是否已对该学生打过分
        GradeEvaluation exist = gradeMapper.selectOne(
                Wrappers.<GradeEvaluation>query()
                        .eq("selection_id", req.getSelectionId())
                        .eq("student_id", req.getStudentId())
                        .eq("evaluator_id", teacher.getUserId())
                        .eq("is_deleted", 0));

        GradeEvaluation entity;
        if (exist != null) {
            // 更新
            entity = exist;
            entity.setRegularScore(req.getRegularScore());
            entity.setThesisScore(req.getThesisScore());
            entity.setDefenseScore(req.getDefenseScore());
            entity.setTotalScore(totalScore);
            entity.setComment(req.getComment());
            entity.setStatus("SUBMITTED");
            entity.setUpdateTime(LocalDateTime.now());
            gradeMapper.updateById(entity);
        } else {
            // 新增
            entity = new GradeEvaluation();
            entity.setSelectionId(req.getSelectionId());
            entity.setStudentId(req.getStudentId());
            entity.setEvaluatorId(teacher.getUserId());
            entity.setEvaluatorName(teacher.getRealName());
            entity.setRegularScore(req.getRegularScore());
            entity.setThesisScore(req.getThesisScore());
            entity.setDefenseScore(req.getDefenseScore());
            entity.setTotalScore(totalScore);
            entity.setComment(req.getComment());
            entity.setStatus("SUBMITTED");
            entity.setIsLocked(0);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            gradeMapper.insert(entity);
        }

        // 重新计算汇总
        refreshSummary(req.getSelectionId(), req.getStudentId());

        // 通知学生
        notifyStudent(sel, entity, teacher);

        return entity.getGradeId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustScore(Long summaryId, BigDecimal adjustedTotalScore, String adjustedGradeLevel, String remark) {
        SysUser admin = SecurityUtils.currentUser();
        if (admin.getUserType() == null || admin.getUserType() != 3) {
            throw new BizException("只有管理员才能调整成绩");
        }
        GradeSummary summary = summaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new BizException("汇总成绩不存在");
        }
        if (adjustedTotalScore != null) {
            summary.setAdjustedTotalScore(adjustedTotalScore);
        }
        if (adjustedGradeLevel != null) {
            summary.setAdjustedGradeLevel(adjustedGradeLevel);
        }
        if (remark != null) {
            summary.setRemark(remark);
        }
        summary.setFinalScore(adjustedTotalScore != null ? adjustedTotalScore : summary.getTotalScore());
        summary.setFinalGradeLevel(adjustedGradeLevel != null ? adjustedGradeLevel : summary.getGradeLevel());
        summary.setIsAdjusted(1);
        summary.setUpdateTime(LocalDateTime.now());
        summaryMapper.updateById(summary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockScore(Long summaryId) {
        SysUser admin = SecurityUtils.currentUser();
        if (admin.getUserType() == null || admin.getUserType() != 3) {
            throw new BizException("只有管理员才能锁定成绩");
        }
        GradeSummary summary = summaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new BizException("汇总成绩不存在");
        }
        if (summary.getIsLocked() != null && summary.getIsLocked() == 1) {
            throw new BizException("该成绩已经锁定，无需重复操作");
        }
        summary.setIsLocked(1);
        summary.setLockTime(LocalDateTime.now());
        summary.setLockedBy(admin.getUserId());
        summaryMapper.updateById(summary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockScore(Long summaryId) {
        SysUser admin = SecurityUtils.currentUser();
        if (admin.getUserType() == null || admin.getUserType() != 3) {
            throw new BizException("只有管理员才能解锁成绩");
        }
        GradeSummary summary = summaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new BizException("汇总成绩不存在");
        }
        summary.setIsLocked(0);
        summary.setLockTime(null);
        summary.setLockedBy(null);
        summaryMapper.updateById(summary);
    }

    @Override
    public GradeResp getDetail(Long summaryId) {
        GradeSummary summary = summaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new BizException("汇总成绩不存在");
        }
        GradeResp resp = buildResp(summary);
        enrichDetailList(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long gradeId) {
        SysUser user = SecurityUtils.currentUser();
        GradeEvaluation entity = gradeMapper.selectById(gradeId);
        if (entity == null) {
            throw new BizException("评分记录不存在");
        }
        // 非管理员只能删除自己的记录
        if (user.getUserType() != 3 && !entity.getEvaluatorId().equals(user.getUserId())) {
            throw new BizException("无权删除此评分记录");
        }
        gradeMapper.deleteById(gradeId);
        // 重新汇总
        refreshSummary(entity.getSelectionId(), entity.getStudentId());
    }

    @Override
    public void exportGrades(String campusName, String academicYear) {
        // 导出功能：查询所有数据，由前端处理 Excel 导出
        // 或通过流式响应返回 Excel 文件
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshSummary(Long selectionId, Long studentId) {
        // 查询所有教师的评分记录
        List<GradeEvaluation> records = gradeMapper.selectList(
                Wrappers.<GradeEvaluation>query()
                        .eq("selection_id", selectionId)
                        .eq("student_id", studentId)
                        .eq("is_deleted", 0)
                        .orderByAsc("evaluator_id"));

        if (records.isEmpty()) {
            return;
        }

        // 计算各维度平均分
        BigDecimal sumRegular = BigDecimal.ZERO;
        BigDecimal sumThesis = BigDecimal.ZERO;
        BigDecimal sumDefense = BigDecimal.ZERO;
        int count = 0;
        for (GradeEvaluation r : records) {
            if (r.getRegularScore() != null) sumRegular = sumRegular.add(r.getRegularScore());
            if (r.getThesisScore() != null) sumThesis = sumThesis.add(r.getThesisScore());
            if (r.getDefenseScore() != null) sumDefense = sumDefense.add(r.getDefenseScore());
            count++;
        }

        BigDecimal avgRegular = count > 0 ? sumRegular.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP) : null;
        BigDecimal avgThesis = count > 0 ? sumThesis.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP) : null;
        BigDecimal avgDefense = count > 0 ? sumDefense.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP) : null;
        BigDecimal totalScore = calcTotalScore(avgRegular, avgThesis, avgDefense);
        String level = calcLevel(totalScore);

        // 查询或创建汇总记录
        GradeSummary summary = summaryMapper.selectOne(
                Wrappers.<GradeSummary>query().eq("selection_id", selectionId).eq("is_deleted", 0));

        if (summary == null) {
            summary = new GradeSummary();
            summary.setSelectionId(selectionId);
            summary.setStudentId(studentId);
            summary.setEvaluatorCount(count);
            summary.setAvgRegularScore(avgRegular);
            summary.setAvgThesisScore(avgThesis);
            summary.setAvgDefenseScore(avgDefense);
            summary.setTotalScore(totalScore);
            summary.setGradeLevel(level);
            summary.setRecordCount(count);
            summary.setFinalScore(totalScore);
            summary.setFinalGradeLevel(level);
            summary.setIsAdjusted(0);
            summary.setIsLocked(0);
            summary.setCreateTime(LocalDateTime.now());
            summary.setUpdateTime(LocalDateTime.now());
            summaryMapper.insert(summary);
        } else {
            // 锁定状态下不更新系统计算值
            if (summary.getIsLocked() == null || summary.getIsLocked() == 0) {
                summary.setEvaluatorCount(count);
                summary.setAvgRegularScore(avgRegular);
                summary.setAvgThesisScore(avgThesis);
                summary.setAvgDefenseScore(avgDefense);
                summary.setTotalScore(totalScore);
                summary.setGradeLevel(level);
                summary.setRecordCount(count);
                summary.setFinalScore(summary.getAdjustedTotalScore() != null
                        ? summary.getAdjustedTotalScore() : totalScore);
                summary.setFinalGradeLevel(summary.getAdjustedGradeLevel() != null
                        ? summary.getAdjustedGradeLevel() : level);
                summary.setUpdateTime(LocalDateTime.now());
                summaryMapper.updateById(summary);
            }
        }
    }

    // ==================== 私有方法 ====================

    private BigDecimal calcTotalScore(BigDecimal regular, BigDecimal thesis, BigDecimal defense) {
        if (regular == null && thesis == null && defense == null) {
            return null;
        }
        BigDecimal r = regular != null ? regular : BigDecimal.ZERO;
        BigDecimal t = thesis != null ? thesis : BigDecimal.ZERO;
        BigDecimal d = defense != null ? defense : BigDecimal.ZERO;
        return r.multiply(WEIGHT_REGULAR)
                .add(t.multiply(WEIGHT_THESIS))
                .add(d.multiply(WEIGHT_DEFENSE))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String calcLevel(BigDecimal score) {
        if (score == null) return null;
        if (score.compareTo(LEVEL_EXCELLENT) >= 0) return "优秀";
        if (score.compareTo(LEVEL_GOOD) >= 0) return "良好";
        if (score.compareTo(LEVEL_MEDIUM) >= 0) return "中等";
        if (score.compareTo(LEVEL_PASS) >= 0) return "及格";
        return "不及格";
    }

    private void enrichDetailList(GradeResp resp) {
        if (resp.getSelectionId() == null || resp.getStudentId() == null) return;
        List<GradeResp.GradeDetailItem> details = gradeMapper.selectDetailByStudent(
                resp.getSelectionId(), resp.getStudentId());
        resp.setDetailList(details);
    }

    private GradeResp buildResp(GradeSummary summary) {
        GradeResp resp = new GradeResp();
        resp.setSummaryId(summary.getSummaryId());
        resp.setSelectionId(summary.getSelectionId());
        resp.setStudentId(summary.getStudentId());
        resp.setEvaluatorCount(summary.getEvaluatorCount());
        resp.setAvgRegularScore(summary.getAvgRegularScore());
        resp.setAvgThesisScore(summary.getAvgThesisScore());
        resp.setAvgDefenseScore(summary.getAvgDefenseScore());
        resp.setTotalScore(summary.getTotalScore());
        resp.setGradeLevel(summary.getGradeLevel());
        resp.setAdjustedTotalScore(summary.getAdjustedTotalScore());
        resp.setFinalScore(summary.getFinalScore());
        resp.setAdjustedGradeLevel(summary.getAdjustedGradeLevel());
        resp.setFinalGradeLevel(summary.getFinalGradeLevel());
        resp.setIsAdjusted(summary.getIsAdjusted() != null && summary.getIsAdjusted() == 1);
        resp.setIsLocked(summary.getIsLocked() != null && summary.getIsLocked() == 1);
        resp.setRemark(summary.getRemark());
        resp.setLockTime(summary.getLockTime());
        resp.setCreateTime(summary.getCreateTime());

        // 填充选题关联信息
        ProjectSelection sel = selectionMapper.selectById(summary.getSelectionId());
        if (sel != null) {
            resp.setAcademicYear(sel.getAcademicYear());
            resp.setCampusName(sel.getCampusName());
            resp.setIsCustomTopic(sel.getIsCustomTopic() != null && sel.getIsCustomTopic() ? 1 : 0);
            resp.setCustomTopicName(sel.getCustomTopicName());
            resp.setTeacherId(sel.getTeacherId());
            if (sel.getTeacherId() != null) {
                SysUser teacher = userMapper.selectById(sel.getTeacherId());
                if (teacher != null) resp.setTeacherName(teacher.getRealName());
            }
        }
        SysUser student = userMapper.selectById(summary.getStudentId());
        if (student != null) {
            resp.setStudentName(student.getRealName());
            resp.setStudentNo(student.getStudentNo());
        }
        return resp;
    }

    private void notifyStudent(ProjectSelection sel, GradeEvaluation grade, SysUser teacher) {
        try {
            NotificationAddReq notification = new NotificationAddReq();
            notification.setTitle("教师已录入您的成绩");
            String comment = grade.getComment();
            String preview = (comment != null && comment.length() > 50) ? comment.substring(0, 50) + "..." : (comment != null ? comment : "无");
            notification.setContent(String.format(
                    "教师「%s」已对您的毕业设计（%s）录入评分：\n" +
                    "平时分：%s | 论文分：%s | 答辩分：%s | 总分：%s\n" +
                    "评语：%s\n请登录系统查看详情。",
                    teacher.getRealName(),
                    sel.getAcademicYear(),
                    grade.getRegularScore() != null ? grade.getRegularScore() : "—",
                    grade.getThesisScore() != null ? grade.getThesisScore() : "—",
                    grade.getDefenseScore() != null ? grade.getDefenseScore() : "—",
                    grade.getTotalScore() != null ? grade.getTotalScore() : "—",
                    preview));
            notification.setNoticeType(2);
            notification.setBizType("GRADE");
            notification.setBizId(grade.getGradeId());
            notification.setReceiverIds(List.of(sel.getStudentId()));
            notificationService.add(notification, teacher.getUserId());
        } catch (Exception ignored) {
        }
    }
}
