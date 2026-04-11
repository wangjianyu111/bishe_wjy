package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysParamMapper {

    Page<SchoolResp> selectSchoolPage(Page<SchoolResp> page, @Param("req") ParamReq req);

    Page<DesignStageResp> selectDesignStagePage(Page<DesignStageResp> page, @Param("req") ParamReq req);

    Page<SysGradeResp> selectGradePage(Page<SysGradeResp> page, @Param("req") ParamReq req);

    Page<TimeSlotResp> selectTimeSlotPage(Page<TimeSlotResp> page, @Param("req") ParamReq req);
}
