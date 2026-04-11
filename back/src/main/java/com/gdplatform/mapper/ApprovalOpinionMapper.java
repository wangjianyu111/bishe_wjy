package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.ApprovalOpinionResp;
import com.gdplatform.entity.ApprovalOpinion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalOpinionMapper extends BaseMapper<ApprovalOpinion> {

    List<ApprovalOpinionResp> selectAdminPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("docType") String docType,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countAdminPage(
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("docType") String docType);

    List<ApprovalOpinionResp> selectTeacherPage(
            @Param("reviewerId") Long reviewerId,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("docType") String docType,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countTeacherPage(
            @Param("reviewerId") Long reviewerId,
            @Param("academicYear") String academicYear,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("docType") String docType);

    List<ApprovalOpinionResp> selectStudentPage(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("status") String status,
            @Param("docType") String docType,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countStudentPage(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("status") String status,
            @Param("docType") String docType);
}
