package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.GuidanceRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GuidanceRelationMapper extends BaseMapper<GuidanceRelation> {

    List<Long> selectStudentIdsByTeacherId(@Param("teacherId") Long teacherId,
                                           @Param("academicYear") String academicYear,
                                           @Param("status") String status);

    Long selectRelationIdByStudentAndYear(@Param("studentId") Long studentId,
                                          @Param("academicYear") String academicYear);
}
