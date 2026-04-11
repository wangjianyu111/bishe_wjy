package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.GradeResp;
import com.gdplatform.entity.GradeEvaluation;
import com.gdplatform.entity.GradeSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成绩评审 - 教师单次评分 Mapper
 */
@Mapper
public interface GradeEvaluationMapper extends BaseMapper<GradeEvaluation> {

    // ==================== 汇总分页查询（管理员） ====================

    List<GradeResp> selectAdminPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("gradeLevel") String gradeLevel,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countAdminPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("gradeLevel") String gradeLevel);

    // ==================== 汇总分页查询（教师：只看本校学生） ====================

    List<GradeResp> selectTeacherPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("gradeLevel") String gradeLevel,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countTeacherPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("gradeLevel") String gradeLevel);

    // ==================== 汇总分页查询（学生：只看自己的） ====================

    List<GradeResp> selectStudentPage(
            @Param("studentId") Long studentId,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countStudentPage(@Param("studentId") Long studentId);

    // ==================== 明细查询（某学生的所有教师评分） ====================

    List<GradeResp.GradeDetailItem> selectDetailByStudent(
            @Param("selectionId") Long selectionId,
            @Param("studentId") Long studentId);
}
