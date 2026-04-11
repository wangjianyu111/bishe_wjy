package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

public interface SysParamService {

    Page<SchoolResp> pageSchools(ParamReq req);
    void addSchool(ParamAddReq req);
    void updateSchool(ParamUpdateReq req);
    void deleteSchool(Long schoolId);

    Page<DesignStageResp> pageDesignStages(ParamReq req);
    void addDesignStage(ParamAddReq req);
    void updateDesignStage(ParamUpdateReq req);
    void deleteDesignStage(Long stageId);

    Page<SysGradeResp> pageGrades(ParamReq req);
    void addGrade(ParamAddReq req);
    void updateGrade(ParamUpdateReq req);
    void deleteGrade(Long gradeId);

    Page<TimeSlotResp> pageTimeSlots(ParamReq req);
    void addTimeSlot(TimeSlotAddReq req);
    void updateTimeSlot(TimeSlotUpdateReq req);
    void deleteTimeSlot(Long slotId);

    void batchUpdate(ParamBatchUpdateReq req);
}
