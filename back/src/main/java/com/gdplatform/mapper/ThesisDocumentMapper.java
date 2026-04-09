package com.gdplatform.mapper;

import com.gdplatform.dto.ThesisPageReq;
import com.gdplatform.dto.ThesisResp;
import com.gdplatform.entity.ThesisDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThesisDocumentMapper {
    ThesisDocument selectById(@Param("thesisId") Long thesisId);
    void insert(ThesisDocument thesis);
    void updateById(ThesisDocument thesis);
    void deleteById(@Param("thesisId") Long thesisId);
    ThesisResp selectBySelectionId(@Param("selectionId") Long selectionId);
    ThesisResp selectLatestByStudentId(@Param("studentId") Long studentId);
    List<ThesisResp> selectThesisPage(@Param("req") ThesisPageReq req,
                                      @Param("offset") long offset, @Param("limit") long limit);
    long countThesisPage(@Param("req") ThesisPageReq req);
    List<ThesisResp> selectThesisPageForTeacher(@Param("req") ThesisPageReq req,
                                                @Param("offset") long offset, @Param("limit") long limit);
    long countThesisPageForTeacher(@Param("req") ThesisPageReq req);
    List<ThesisResp> selectAllByStudentId(@Param("studentId") Long studentId);
}