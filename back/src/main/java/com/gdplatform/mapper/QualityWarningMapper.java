package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.WarningResp;
import com.gdplatform.entity.QualityWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QualityWarningMapper extends BaseMapper<QualityWarning> {

    IPage<WarningResp> selectPageForAdmin(Page<?> page,
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("warnType") String warnType,
            @Param("warnLevel") Integer warnLevel,
            @Param("status") String status,
            @Param("keyword") String keyword);

    long countPageForAdmin(@Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("warnType") String warnType,
            @Param("warnLevel") Integer warnLevel,
            @Param("status") String status,
            @Param("keyword") String keyword);

    IPage<WarningResp> selectPageForTeacher(Page<?> page,
            @Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("warnType") String warnType,
            @Param("warnLevel") Integer warnLevel,
            @Param("status") String status);

    long countPageForTeacher(@Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("warnType") String warnType,
            @Param("warnLevel") Integer warnLevel,
            @Param("status") String status);

    long countByConditions(@Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("warnType") String warnType,
            @Param("warnLevel") Integer warnLevel,
            @Param("status") String status);
}
