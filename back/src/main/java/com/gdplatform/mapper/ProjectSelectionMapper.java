package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.SelectionPageReq;
import com.gdplatform.dto.SelectionResp;
import com.gdplatform.entity.ProjectSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectSelectionMapper extends BaseMapper<ProjectSelection> {

    List<SelectionResp> selectRespPage(@Param("req") SelectionPageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countRespPage(@Param("req") SelectionPageReq req);

    List<SelectionResp> selectForTeacher(@Param("req") SelectionPageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countForTeacher(@Param("req") SelectionPageReq req);
}
