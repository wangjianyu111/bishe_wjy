package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import com.gdplatform.entity.SysDesignStage;
import com.gdplatform.entity.SysGrade;
import com.gdplatform.entity.SysSchool;
import com.gdplatform.entity.SysTimeSlot;
import com.gdplatform.mapper.*;
import com.gdplatform.service.SysParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysParamServiceImpl implements SysParamService {

    private final SysParamMapper paramMapper;
    private final SysSchoolMapper schoolMapper;
    private final SysDesignStageMapper designStageMapper;
    private final SysGradeMapper gradeMapper;
    private final SysTimeSlotMapper timeSlotMapper;

    // ==================== 学校 ====================
    @Override
    public Page<SchoolResp> pageSchools(ParamReq req) {
        Page<SchoolResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<SchoolResp> result = paramMapper.selectSchoolPage(page, req);
        result.getRecords().forEach(this::enrichSchoolLabels);
        return result;
    }

    @Override
    public void addSchool(ParamAddReq req) {
        SysSchool s = new SysSchool();
        s.setSchoolName(req.getName());
        s.setSchoolCode(req.getCode());
        s.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        schoolMapper.insert(s);
    }

    @Override
    public void updateSchool(ParamUpdateReq req) {
        SysSchool s = new SysSchool();
        s.setSchoolId(req.getId());
        s.setSchoolName(req.getName());
        s.setSchoolCode(req.getCode());
        s.setStatus(req.getStatus());
        s.setUpdateTime(LocalDateTime.now());
        schoolMapper.updateById(s);
    }

    @Override
    public void deleteSchool(Long schoolId) {
        schoolMapper.deleteById(schoolId);
    }

    // ==================== 毕业设计阶段 ====================
    @Override
    public Page<DesignStageResp> pageDesignStages(ParamReq req) {
        Page<DesignStageResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<DesignStageResp> result = paramMapper.selectDesignStagePage(page, req);
        result.getRecords().forEach(this::enrichStageLabels);
        return result;
    }

    @Override
    public void addDesignStage(ParamAddReq req) {
        SysDesignStage s = new SysDesignStage();
        s.setStageName(req.getName());
        s.setSchoolId(req.getSchoolId());
        s.setStageOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        s.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        designStageMapper.insert(s);
    }

    @Override
    public void updateDesignStage(ParamUpdateReq req) {
        SysDesignStage s = new SysDesignStage();
        s.setStageId(req.getId());
        s.setStageName(req.getName());
        s.setSchoolId(req.getSchoolId());
        s.setStageOrder(req.getSortOrder());
        s.setStatus(req.getStatus());
        s.setUpdateTime(LocalDateTime.now());
        designStageMapper.updateById(s);
    }

    @Override
    public void deleteDesignStage(Long stageId) {
        designStageMapper.deleteById(stageId);
    }

    // ==================== 年级 ====================
    @Override
    public Page<SysGradeResp> pageGrades(ParamReq req) {
        Page<SysGradeResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<SysGradeResp> result = paramMapper.selectGradePage(page, req);
        result.getRecords().forEach(this::enrichGradeLabels);
        return result;
    }

    @Override
    public void addGrade(ParamAddReq req) {
        SysGrade g = new SysGrade();
        g.setGradeName(req.getName());
        g.setSchoolId(req.getSchoolId());
        g.setGradeYear(req.getCode() != null ? Integer.parseInt(req.getCode()) : null);
        g.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        g.setCreateTime(LocalDateTime.now());
        g.setUpdateTime(LocalDateTime.now());
        gradeMapper.insert(g);
    }

    @Override
    public void updateGrade(ParamUpdateReq req) {
        SysGrade g = new SysGrade();
        g.setGradeId(req.getId());
        g.setGradeName(req.getName());
        g.setSchoolId(req.getSchoolId());
        g.setGradeYear(req.getCode() != null ? Integer.parseInt(req.getCode()) : null);
        g.setStatus(req.getStatus());
        g.setUpdateTime(LocalDateTime.now());
        gradeMapper.updateById(g);
    }

    @Override
    public void deleteGrade(Long gradeId) {
        gradeMapper.deleteById(gradeId);
    }

    // ==================== 时间段 ====================
    @Override
    public Page<TimeSlotResp> pageTimeSlots(ParamReq req) {
        Page<TimeSlotResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<TimeSlotResp> result = paramMapper.selectTimeSlotPage(page, req);
        result.getRecords().forEach(this::enrichTimeSlotLabels);
        return result;
    }

    @Override
    public void addTimeSlot(TimeSlotAddReq req) {
        SysTimeSlot t = new SysTimeSlot();
        t.setSlotName(req.getSlotName());
        t.setSchoolId(req.getSchoolId());
        t.setSlotType(req.getSlotType());
        t.setStartDate(req.getStartDate());
        t.setEndDate(req.getEndDate());
        t.setSlotOrder(req.getSlotOrder() != null ? req.getSlotOrder() : 0);
        t.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        timeSlotMapper.insert(t);
    }

    @Override
    public void updateTimeSlot(TimeSlotUpdateReq req) {
        SysTimeSlot t = new SysTimeSlot();
        t.setSlotId(req.getSlotId());
        t.setSlotName(req.getSlotName());
        t.setSchoolId(req.getSchoolId());
        t.setSlotType(req.getSlotType());
        t.setStartDate(req.getStartDate());
        t.setEndDate(req.getEndDate());
        t.setSlotOrder(req.getSlotOrder());
        t.setStatus(req.getStatus());
        t.setUpdateTime(LocalDateTime.now());
        timeSlotMapper.updateById(t);
    }

    @Override
    public void deleteTimeSlot(Long slotId) {
        timeSlotMapper.deleteById(slotId);
    }

    // ==================== 批量更新 ====================
    @Override
    @Transactional
    public void batchUpdate(ParamBatchUpdateReq req) {
        for (ParamUpdateReq item : req.getItems()) {
            if ("SCHOOL".equals(req.getType())) {
                updateSchool(item);
            } else if ("DESIGN_STAGE".equals(req.getType())) {
                updateDesignStage(item);
            } else if ("GRADE".equals(req.getType())) {
                updateGrade(item);
            }
        }
    }

    // ==================== 标签转换 ====================
    private void enrichSchoolLabels(SchoolResp r) {
        r.setStatusLabel(r.getStatus() != null && r.getStatus() == 1 ? "启用" : "禁用");
    }

    private void enrichStageLabels(DesignStageResp r) {
        r.setStatusLabel(r.getStatus() != null && r.getStatus() == 1 ? "启用" : "禁用");
    }

    private void enrichGradeLabels(SysGradeResp r) {
        r.setStatusLabel(r.getStatus() != null && r.getStatus() == 1 ? "启用" : "禁用");
    }

    private void enrichTimeSlotLabels(TimeSlotResp r) {
        r.setStatusLabel(r.getStatus() != null && r.getStatus() == 1 ? "启用" : "禁用");
        r.setSlotTypeLabel(switch (r.getSlotType()) {
            case "THESIS_SUBMISSION" -> "论文提交";
            case "DEFENSE" -> "答辩";
            case "SCORE" -> "成绩评定";
            case "ARCHIVE" -> "归档";
            default -> r.getSlotType();
        });
    }
}
