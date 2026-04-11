package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.DefenseArrangeResp;
import com.gdplatform.dto.DefenseSessionResp;
import com.gdplatform.entity.DefenseSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DefenseSessionMapper extends BaseMapper<DefenseSession> {

    @Select("SELECT * FROM defense_session WHERE session_id = #{sessionId} AND is_deleted = 0")
    DefenseSession selectBySessionId(@Param("sessionId") Long sessionId);

    List<DefenseSessionResp> selectSessionPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countSessionPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword);

    List<DefenseSessionResp> selectSessionPageForTeacher(
            @Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countSessionPageForTeacher(
            @Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword);

    List<DefenseSessionResp> selectSessionPageForStudent(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countSessionPageForStudent(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear);

    List<DefenseArrangeResp> selectArrangementPage(
            @Param("sessionId") Long sessionId,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countArrangementPage(
            @Param("sessionId") Long sessionId,
            @Param("keyword") String keyword);
}
