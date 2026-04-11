package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.TeacherFeedbackResp;
import com.gdplatform.entity.TeacherFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherFeedbackMapper extends BaseMapper<TeacherFeedback> {

    List<TeacherFeedbackResp> selectPageForAdmin(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("feedbackType") String feedbackType,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countPageForAdmin(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("feedbackType") String feedbackType,
            @Param("status") String status,
            @Param("keyword") String keyword);

    List<TeacherFeedbackResp> selectPageForTeacher(
            @Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("feedbackType") String feedbackType,
            @Param("status") String status,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countPageForTeacher(
            @Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear,
            @Param("feedbackType") String feedbackType,
            @Param("status") String status);
}
