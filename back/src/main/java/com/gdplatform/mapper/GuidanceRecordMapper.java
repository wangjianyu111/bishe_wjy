package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.GuidanceRecordResp;
import com.gdplatform.dto.SelectionForGuidanceResp;
import com.gdplatform.entity.GuidanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GuidanceRecordMapper extends BaseMapper<GuidanceRecord> {

    void insertWithAcademicYear(GuidanceRecord record);

    List<SelectionForGuidanceResp> selectSelectionsForTeacher(@Param("teacherId") Long teacherId);

    List<GuidanceRecordResp> selectPageForAdmin(@Param("campusName") String campusName,
                                                @Param("academicYear") String academicYear,
                                                @Param("keyword") String keyword,
                                                @Param("teacherId") Long teacherId,
                                                @Param("offset") long offset,
                                                @Param("limit") long limit);

    long countPageForAdmin(@Param("campusName") String campusName,
                           @Param("academicYear") String academicYear,
                           @Param("keyword") String keyword,
                           @Param("teacherId") Long teacherId);

    List<GuidanceRecordResp> selectPageForTeacher(@Param("campusName") String campusName,
                                                   @Param("academicYear") String academicYear,
                                                   @Param("keyword") String keyword,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    long countPageForTeacher(@Param("campusName") String campusName,
                              @Param("academicYear") String academicYear,
                              @Param("keyword") String keyword);

    List<GuidanceRecordResp> selectPageForStudent(@Param("studentId") Long studentId,
                                                   @Param("academicYear") String academicYear,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    long countPageForStudent(@Param("studentId") Long studentId,
                              @Param("academicYear") String academicYear);

    @Select("SELECT COUNT(*) FROM guidance_record WHERE student_id = #{studentId} AND is_deleted = 0")
    int countByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT COUNT(*) FROM guidance_record WHERE student_id = #{studentId} AND is_deleted = 0 " +
            "AND academic_year = #{academicYear}")
    int countByStudentIdAndYear(@Param("studentId") Long studentId, @Param("academicYear") String academicYear);

    @Select("SELECT guide_time FROM guidance_record WHERE student_id = #{studentId} AND is_deleted = 0 " +
            "ORDER BY guide_time DESC LIMIT 1")
    String findLastGuideTime(@Param("studentId") Long studentId);

    @Update("UPDATE guidance_record SET student_feedback = #{feedback}, feedback_time = NOW() WHERE guide_id = #{guideId}")
    void updateFeedback(@Param("guideId") Long guideId, @Param("feedback") String feedback);

    @Select("SELECT * FROM guidance_record WHERE guide_id = #{guideId} AND is_deleted = 0")
    GuidanceRecord selectByGuideId(@Param("guideId") Long guideId);
}
