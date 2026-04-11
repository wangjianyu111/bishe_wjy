package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.CampusThresholdReq;
import com.gdplatform.dto.ExcellentAchievementReq;
import com.gdplatform.dto.ExcellentAchievementResp;
import com.gdplatform.dto.GradeOptionResp;
import com.gdplatform.entity.CampusThreshold;
import com.gdplatform.entity.ExcellentAchievement;
import com.gdplatform.entity.GradeSummary;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.CampusThresholdMapper;
import com.gdplatform.mapper.ExcellentAchievementMapper;
import com.gdplatform.mapper.GradeSummaryMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.service.ExcellentAchievementService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcellentAchievementServiceImpl implements ExcellentAchievementService {

    private final ExcellentAchievementMapper excellentMapper;
    private final GradeSummaryMapper summaryMapper;
    private final CampusThresholdMapper thresholdMapper;
    private final ProjectSelectionMapper selectionMapper;

    @Override
    public Page<ExcellentAchievementResp> adminPage(long current, long size,
                                                   String campusName, String academicYear, String keyword) {
        Page<ExcellentAchievementResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ExcellentAchievementResp> records = excellentMapper.selectAdminPage(
                campusName, academicYear, keyword, offset, size);
        for (ExcellentAchievementResp r : records) {
            r.setIsExported(Boolean.TRUE.equals(r.getIsExported()));
            r.setIsAdjusted(Boolean.TRUE.equals(r.getIsAdjusted()));
            r.setIsLocked(Boolean.TRUE.equals(r.getIsLocked()));
        }
        long total = excellentMapper.countAdminPage(campusName, academicYear, keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ExcellentAchievementResp> teacherPage(long current, long size,
                                                      String campusName, String academicYear, String keyword) {
        SysUser teacher = SecurityUtils.currentUser();
        if (teacher.getCampusName() == null) {
            throw new BizException("当前用户未设置学校，无法查询");
        }
        Page<ExcellentAchievementResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ExcellentAchievementResp> records = excellentMapper.selectTeacherPage(
                teacher.getCampusName(), academicYear, keyword, offset, size);
        for (ExcellentAchievementResp r : records) {
            r.setIsExported(Boolean.TRUE.equals(r.getIsExported()));
            r.setIsAdjusted(Boolean.TRUE.equals(r.getIsAdjusted()));
            r.setIsLocked(Boolean.TRUE.equals(r.getIsLocked()));
        }
        long total = excellentMapper.countTeacherPage(teacher.getCampusName(), academicYear, keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ExcellentAchievementResp> studentPage(long current, long size) {
        SysUser student = SecurityUtils.currentUser();
        Page<ExcellentAchievementResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ExcellentAchievementResp> records = excellentMapper.selectStudentPage(
                student.getUserId(), offset, size);
        for (ExcellentAchievementResp r : records) {
            r.setIsExported(Boolean.TRUE.equals(r.getIsExported()));
            r.setIsAdjusted(Boolean.TRUE.equals(r.getIsAdjusted()));
            r.setIsLocked(Boolean.TRUE.equals(r.getIsLocked()));
        }
        long total = excellentMapper.countStudentPage(student.getUserId());
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<ExcellentAchievementResp> studentSelfPage(long current, long size, String keyword) {
        SysUser student = SecurityUtils.currentUser();
        if (student.getCampusName() == null) {
            throw new BizException("当前用户未设置学校，无法查询");
        }
        Page<ExcellentAchievementResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<ExcellentAchievementResp> records = excellentMapper.selectStudentSelf(
                student.getCampusName(), keyword, offset, size);
        for (ExcellentAchievementResp r : records) {
            r.setIsExported(Boolean.TRUE.equals(r.getIsExported()));
            r.setIsAdjusted(Boolean.TRUE.equals(r.getIsAdjusted()));
            r.setIsLocked(Boolean.TRUE.equals(r.getIsLocked()));
        }
        long total = excellentMapper.countStudentSelf(student.getCampusName(), keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ExcellentAchievementReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || (user.getUserType() != 2 && user.getUserType() != 3)) {
            throw new BizException("只有教师或管理员才能认定优秀成果");
        }

        GradeSummary summary = summaryMapper.selectById(req.getGradeSummaryId());
        if (summary == null) {
            throw new BizException("成绩汇总记录不存在");
        }
        if (summary.getTotalScore() == null) {
            throw new BizException("该学生暂无有效成绩，无法认定");
        }

        // 检查是否已有认定记录
        ExcellentAchievement exist = excellentMapper.selectOne(
                Wrappers.<ExcellentAchievement>query()
                        .eq("grade_summary_id", req.getGradeSummaryId())
                        .eq("is_deleted", 0));
        if (exist != null) {
            throw new BizException("该学生已有优秀成果认定记录，请撤销后重新认定");
        }

        // 查询该学校的优秀阈值
        CampusThreshold threshold = getThreshold(summary);
        if (threshold == null) {
            throw new BizException("该学校/学年未设置优秀分数线，请先在阈值配置中设置");
        }
        if (summary.getTotalScore().compareTo(threshold.getExcellentScore()) <= 0) {
            throw new BizException(String.format("该学生成绩（%.2f）未达到优秀分数线（%.2f），无法认定",
                    summary.getTotalScore(), threshold.getExcellentScore()));
        }

        ExcellentAchievement entity = new ExcellentAchievement();
        entity.setGradeSummaryId(req.getGradeSummaryId());
        entity.setSelectionId(summary.getSelectionId());
        entity.setStudentId(summary.getStudentId());
        entity.setFinalScore(summary.getTotalScore());
        entity.setFinalGradeLevel("优秀");
        entity.setStatus("APPROVED");
        entity.setApproverId(user.getUserId());
        entity.setApproverName(user.getRealName());
        entity.setRemark(req.getRemark());
        entity.setIsExported(0);
        entity.setApproveTime(LocalDateTime.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        excellentMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long excellentId) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == null || (user.getUserType() != 2 && user.getUserType() != 3)) {
            throw new BizException("只有教师或管理员才能撤销认定");
        }
        ExcellentAchievement entity = excellentMapper.selectById(excellentId);
        if (entity == null) {
            throw new BizException("认定记录不存在");
        }
        if ("PENDING".equals(entity.getStatus())) {
            excellentMapper.deleteById(excellentId);
        } else {
            entity.setStatus("REJECTED");
            entity.setRemark("已撤销认定");
            entity.setUpdateTime(LocalDateTime.now());
            excellentMapper.updateById(entity);
        }
    }

    @Override
    public List<CampusThreshold> listThresholds(String campusName, String academicYear) {
        return thresholdMapper.selectListByCondition(campusName, academicYear);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveThreshold(CampusThresholdReq req) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() != 3) {
            throw new BizException("只有管理员才能设置优秀分数线");
        }
        if (req.getExcellentScore() == null || req.getExcellentScore().compareTo(BigDecimal.ZERO) <= 0
                || req.getExcellentScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BizException("优秀分数线必须在 0~100 之间");
        }
        if (req.getThresholdId() != null) {
            // 更新
            CampusThreshold existing = thresholdMapper.selectById(req.getThresholdId());
            if (existing == null) {
                throw new BizException("阈值记录不存在");
            }
            existing.setExcellentScore(req.getExcellentScore());
            existing.setRemark(req.getRemark());
            existing.setUpdateTime(LocalDateTime.now());
            thresholdMapper.updateById(existing);
        } else {
            // 新增：检查是否已存在相同学校+学年
            CampusThreshold exist = thresholdMapper.selectOne(
                    Wrappers.<CampusThreshold>query()
                            .eq("campus_name", req.getCampusName())
                            .eq("academic_year", req.getAcademicYear())
                            .eq("is_deleted", 0));
            if (exist != null) {
                throw new BizException("该学校/学年已设置过优秀分数线，请使用编辑功能修改");
            }
            CampusThreshold entity = new CampusThreshold();
            entity.setCampusName(req.getCampusName());
            entity.setAcademicYear(req.getAcademicYear());
            entity.setExcellentScore(req.getExcellentScore());
            entity.setRemark(req.getRemark());
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            thresholdMapper.insert(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThreshold(Long thresholdId) {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() != 3) {
            throw new BizException("只有管理员才能删除阈值配置");
        }
        thresholdMapper.deleteById(thresholdId);
    }

    @Override
    public List<GradeOptionResp> listQualifiedGrades(String campusName) {
        return excellentMapper.selectQualifiedGrades(campusName);
    }

    @Override
    public List<ExcellentAchievementResp> getExportData(String campusName, String academicYear) {
        List<ExcellentAchievementResp> list = excellentMapper.selectForExport(campusName, academicYear);
        for (ExcellentAchievementResp r : list) {
            r.setIsExported(Boolean.TRUE.equals(r.getIsExported()));
            r.setIsAdjusted(Boolean.TRUE.equals(r.getIsAdjusted()));
            r.setIsLocked(Boolean.TRUE.equals(r.getIsLocked()));
        }
        return list;
    }

    @Override
    public void exportExcel(String campusName, String academicYear) {
        getExportData(campusName, academicYear);
    }

    // ==================== 私有方法 ====================

    private CampusThreshold getThreshold(GradeSummary summary) {
        ProjectSelection sel = selectionMapper.selectById(summary.getSelectionId());
        if (sel == null) return null;
        return thresholdMapper.selectOne(
                Wrappers.<CampusThreshold>query()
                        .eq("campus_name", sel.getCampusName())
                        .eq("academic_year", sel.getAcademicYear())
                        .eq("is_deleted", 0));
    }
}
