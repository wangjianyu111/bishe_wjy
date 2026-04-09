package com.gdplatform.mapper;

import com.gdplatform.dto.MidTermPageReq;
import com.gdplatform.dto.MidTermResp;
import com.gdplatform.entity.ProjectMidTerm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMidTermMapper {

    ProjectMidTerm selectById(@Param("midId") Long midId);

    void insert(ProjectMidTerm midTerm);

    void updateById(ProjectMidTerm midTerm);

    void deleteById(@Param("midId") Long midId);

    MidTermResp selectBySelectionId(@Param("selectionId") Long selectionId);

    List<MidTermResp> selectMidTermPage(@Param("req") MidTermPageReq req,
                                         @Param("offset") long offset,
                                         @Param("limit") long limit);

    long countMidTermPage(@Param("req") MidTermPageReq req);

    List<MidTermResp> selectMidTermPageForTeacher(@Param("req") MidTermPageReq req,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    long countMidTermPageForTeacher(@Param("req") MidTermPageReq req);

    List<MidTermResp> selectMidTermPageForStudent(@Param("req") MidTermPageReq req,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    long countMidTermPageForStudent(@Param("req") MidTermPageReq req);
}
