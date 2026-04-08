package com.gdplatform.mapper;

import com.gdplatform.dto.ProgressPageReq;
import com.gdplatform.dto.ProgressResp;
import com.gdplatform.entity.ProjectProgress;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectProgressMapper {

    ProjectProgress selectById(@Param("progressId") Long progressId);

    void insert(ProjectProgress progress);

    void updateById(ProjectProgress progress);

    void deleteById(@Param("progressId") Long progressId);

    List<ProgressResp> selectProgressPage(@Param("req") ProgressPageReq req,
                                         @Param("offset") long offset,
                                         @Param("limit") long limit);

    long countProgressPage(@Param("req") ProgressPageReq req);

    List<ProgressResp> selectProgressPageForTeacher(@Param("req") ProgressPageReq req,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    long countProgressPageForTeacher(@Param("req") ProgressPageReq req);

    List<ProgressResp> selectProgressPageForStudent(@Param("req") ProgressPageReq req,
                                                    @Param("offset") long offset,
                                                    @Param("limit") long limit);

    long countProgressPageForStudent(@Param("req") ProgressPageReq req);
}
