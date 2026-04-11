package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.ExcellentAchievementResp;
import com.gdplatform.dto.GradeOptionResp;
import com.gdplatform.entity.ExcellentAchievement;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExcellentAchievementMapper extends BaseMapper<ExcellentAchievement> {

    // ==================== 管理员分页查询 ====================

    @Select("SELECT COUNT(ea.excellent_id) " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 " +
            "AND (#{campusName} IS NULL OR #{campusName} = '' OR ps.campus_name = #{campusName}) " +
            "AND (#{academicYear} IS NULL OR #{academicYear} = '' OR ps.academic_year = #{academicYear}) " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.student_no LIKE CONCAT('%', #{keyword}, '%'))")
    long countAdminPage(@Param("campusName") String campusName,
                        @Param("academicYear") String academicYear,
                        @Param("keyword") String keyword);

    @Select("SELECT ea.excellent_id AS excellentId, ea.grade_summary_id AS gradeSummaryId, " +
            "ea.selection_id AS selectionId, ea.student_id AS studentId, " +
            "ea.final_score AS finalScore, ea.final_grade_level AS finalGradeLevel, " +
            "ea.status, ea.approver_id AS approverId, ea.approver_name AS approverName, " +
            "ea.remark, ea.is_exported AS isExported, ea.approve_time AS approveTime, ea.create_time AS createTime, " +
            "su.real_name AS studentName, su.student_no AS studentNo, " +
            "CASE WHEN ps.is_custom_topic = 1 THEN ps.custom_topic_name ELSE pt.topic_name END AS topicName, " +
            "ps.custom_topic_name AS customTopicName, ps.is_custom_topic AS isCustomTopic, " +
            "ps.teacher_id AS teacherId, tu.real_name AS teacherName, " +
            "ps.campus_name AS campusName, ps.academic_year AS academicYear, " +
            "gs.total_score AS gradeTotalScore, gs.final_grade_level AS gradeFinalGradeLevel, " +
            "gs.avg_regular_score AS avgRegularScore, gs.avg_thesis_score AS avgThesisScore, " +
            "gs.avg_defense_score AS avgDefenseScore, gs.evaluator_count AS evaluatorCount, " +
            "gs.is_adjusted AS isAdjusted, gs.is_locked AS isLocked, " +
            "ct.excellent_score AS campusThreshold " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "LEFT JOIN project_topic pt ON pt.topic_id = ps.topic_id AND pt.is_deleted = 0 " +
            "LEFT JOIN sys_user tu ON ps.teacher_id = tu.user_id AND tu.is_deleted = 0 " +
            "LEFT JOIN campus_threshold ct ON ct.campus_name = ps.campus_name " +
            "  AND ct.academic_year = ps.academic_year AND ct.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 " +
            "AND (#{campusName} IS NULL OR #{campusName} = '' OR ps.campus_name = #{campusName}) " +
            "AND (#{academicYear} IS NULL OR #{academicYear} = '' OR ps.academic_year = #{academicYear}) " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.student_no LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY ea.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ExcellentAchievementResp> selectAdminPage(@Param("campusName") String campusName,
                                                   @Param("academicYear") String academicYear,
                                                   @Param("keyword") String keyword,
                                                   @Param("offset") long offset,
                                                   @Param("limit") long limit);

    // ==================== 教师分页查询（本校） ====================

    @Select("SELECT COUNT(ea.excellent_id) " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 " +
            "AND ps.campus_name = #{campusName} " +
            "AND (#{academicYear} IS NULL OR #{academicYear} = '' OR ps.academic_year = #{academicYear}) " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.student_no LIKE CONCAT('%', #{keyword}, '%'))")
    long countTeacherPage(@Param("campusName") String campusName,
                          @Param("academicYear") String academicYear,
                          @Param("keyword") String keyword);

    @Select("SELECT ea.excellent_id AS excellentId, ea.grade_summary_id AS gradeSummaryId, " +
            "ea.selection_id AS selectionId, ea.student_id AS studentId, " +
            "ea.final_score AS finalScore, ea.final_grade_level AS finalGradeLevel, " +
            "ea.status, ea.approver_id AS approverId, ea.approver_name AS approverName, " +
            "ea.remark, ea.is_exported AS isExported, ea.approve_time AS approveTime, ea.create_time AS createTime, " +
            "su.real_name AS studentName, su.student_no AS studentNo, " +
            "CASE WHEN ps.is_custom_topic = 1 THEN ps.custom_topic_name ELSE pt.topic_name END AS topicName, " +
            "ps.custom_topic_name AS customTopicName, ps.is_custom_topic AS isCustomTopic, " +
            "ps.teacher_id AS teacherId, tu.real_name AS teacherName, " +
            "ps.campus_name AS campusName, ps.academic_year AS academicYear, " +
            "gs.total_score AS gradeTotalScore, gs.final_grade_level AS gradeFinalGradeLevel, " +
            "gs.avg_regular_score AS avgRegularScore, gs.avg_thesis_score AS avgThesisScore, " +
            "gs.avg_defense_score AS avgDefenseScore, gs.evaluator_count AS evaluatorCount, " +
            "gs.is_adjusted AS isAdjusted, gs.is_locked AS isLocked, " +
            "ct.excellent_score AS campusThreshold " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "LEFT JOIN project_topic pt ON pt.topic_id = ps.topic_id AND pt.is_deleted = 0 " +
            "LEFT JOIN sys_user tu ON ps.teacher_id = tu.user_id AND tu.is_deleted = 0 " +
            "LEFT JOIN campus_threshold ct ON ct.campus_name = ps.campus_name " +
            "  AND ct.academic_year = ps.academic_year AND ct.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 " +
            "AND ps.campus_name = #{campusName} " +
            "AND (#{academicYear} IS NULL OR #{academicYear} = '' OR ps.academic_year = #{academicYear}) " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.student_no LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY ea.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ExcellentAchievementResp> selectTeacherPage(@Param("campusName") String campusName,
                                                     @Param("academicYear") String academicYear,
                                                     @Param("keyword") String keyword,
                                                     @Param("offset") long offset,
                                                     @Param("limit") long limit);

    // ==================== 学生分页查询（自己的） ====================

    @Select("SELECT COUNT(ea.excellent_id) " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 AND ea.student_id = #{studentId}")
    long countStudentPage(@Param("studentId") Long studentId);

    @Select("SELECT ea.excellent_id AS excellentId, ea.grade_summary_id AS gradeSummaryId, " +
            "ea.selection_id AS selectionId, ea.student_id AS studentId, " +
            "ea.final_score AS finalScore, ea.final_grade_level AS finalGradeLevel, " +
            "ea.status, ea.approver_id AS approverId, ea.approver_name AS approverName, " +
            "ea.remark, ea.is_exported AS isExported, ea.approve_time AS approveTime, ea.create_time AS createTime, " +
            "su.real_name AS studentName, su.student_no AS studentNo, " +
            "CASE WHEN ps.is_custom_topic = 1 THEN ps.custom_topic_name ELSE pt.topic_name END AS topicName, " +
            "ps.custom_topic_name AS customTopicName, ps.is_custom_topic AS isCustomTopic, " +
            "ps.teacher_id AS teacherId, tu.real_name AS teacherName, " +
            "ps.campus_name AS campusName, ps.academic_year AS academicYear, " +
            "gs.total_score AS gradeTotalScore, gs.final_grade_level AS gradeFinalGradeLevel, " +
            "gs.avg_regular_score AS avgRegularScore, gs.avg_thesis_score AS avgThesisScore, " +
            "gs.avg_defense_score AS avgDefenseScore, gs.evaluator_count AS evaluatorCount, " +
            "gs.is_adjusted AS isAdjusted, gs.is_locked AS isLocked, " +
            "ct.excellent_score AS campusThreshold " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "LEFT JOIN project_topic pt ON pt.topic_id = ps.topic_id AND pt.is_deleted = 0 " +
            "LEFT JOIN sys_user tu ON ps.teacher_id = tu.user_id AND tu.is_deleted = 0 " +
            "LEFT JOIN campus_threshold ct ON ct.campus_name = ps.campus_name " +
            "  AND ct.academic_year = ps.academic_year AND ct.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 AND ea.student_id = #{studentId} " +
            "ORDER BY ea.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ExcellentAchievementResp> selectStudentPage(@Param("studentId") Long studentId,
                                                     @Param("offset") long offset,
                                                     @Param("limit") long limit);

    // ==================== 学生自助查询（本校名单） ====================

    @Select("SELECT COUNT(ea.excellent_id) " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 " +
            "AND ps.campus_name = #{campusName} " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.student_no LIKE CONCAT('%', #{keyword}, '%'))")
    long countStudentSelf(@Param("campusName") String campusName, @Param("keyword") String keyword);

    @Select("SELECT ea.excellent_id AS excellentId, ea.grade_summary_id AS gradeSummaryId, " +
            "ea.selection_id AS selectionId, ea.student_id AS studentId, " +
            "ea.final_score AS finalScore, ea.final_grade_level AS finalGradeLevel, " +
            "ea.status, ea.approver_id AS approverId, ea.approver_name AS approverName, " +
            "ea.remark, ea.is_exported AS isExported, ea.approve_time AS approveTime, ea.create_time AS createTime, " +
            "su.real_name AS studentName, su.student_no AS studentNo, " +
            "CASE WHEN ps.is_custom_topic = 1 THEN ps.custom_topic_name ELSE pt.topic_name END AS topicName, " +
            "ps.custom_topic_name AS customTopicName, ps.is_custom_topic AS isCustomTopic, " +
            "ps.teacher_id AS teacherId, tu.real_name AS teacherName, " +
            "ps.campus_name AS campusName, ps.academic_year AS academicYear, " +
            "gs.total_score AS gradeTotalScore, gs.final_grade_level AS gradeFinalGradeLevel, " +
            "gs.avg_regular_score AS avgRegularScore, gs.avg_thesis_score AS avgThesisScore, " +
            "gs.avg_defense_score AS avgDefenseScore, gs.evaluator_count AS evaluatorCount, " +
            "gs.is_adjusted AS isAdjusted, gs.is_locked AS isLocked, " +
            "ct.excellent_score AS campusThreshold " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "LEFT JOIN project_topic pt ON pt.topic_id = ps.topic_id AND pt.is_deleted = 0 " +
            "LEFT JOIN sys_user tu ON ps.teacher_id = tu.user_id AND tu.is_deleted = 0 " +
            "LEFT JOIN campus_threshold ct ON ct.campus_name = ps.campus_name " +
            "  AND ct.academic_year = ps.academic_year AND ct.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 " +
            "AND ps.campus_name = #{campusName} " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR su.real_name LIKE CONCAT('%', #{keyword}, '%') OR su.student_no LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY ea.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ExcellentAchievementResp> selectStudentSelf(@Param("campusName") String campusName,
                                                      @Param("keyword") String keyword,
                                                      @Param("offset") long offset,
                                                      @Param("limit") long limit);

    // ==================== 导出查询 ====================

    @Select("SELECT ea.excellent_id AS excellentId, ea.grade_summary_id AS gradeSummaryId, " +
            "ea.selection_id AS selectionId, ea.student_id AS studentId, " +
            "ea.final_score AS finalScore, ea.final_grade_level AS finalGradeLevel, " +
            "ea.status, ea.approver_id AS approverId, ea.approver_name AS approverName, " +
            "ea.remark, ea.is_exported AS isExported, ea.approve_time AS approveTime, ea.create_time AS createTime, " +
            "su.real_name AS studentName, su.student_no AS studentNo, " +
            "CASE WHEN ps.is_custom_topic = 1 THEN ps.custom_topic_name ELSE pt.topic_name END AS topicName, " +
            "ps.custom_topic_name AS customTopicName, ps.is_custom_topic AS isCustomTopic, " +
            "ps.teacher_id AS teacherId, tu.real_name AS teacherName, " +
            "ps.campus_name AS campusName, ps.academic_year AS academicYear, " +
            "gs.total_score AS gradeTotalScore, gs.final_grade_level AS gradeFinalGradeLevel, " +
            "gs.avg_regular_score AS avgRegularScore, gs.avg_thesis_score AS avgThesisScore, " +
            "gs.avg_defense_score AS avgDefenseScore, gs.evaluator_count AS evaluatorCount, " +
            "gs.is_adjusted AS isAdjusted, gs.is_locked AS isLocked, " +
            "ct.excellent_score AS campusThreshold " +
            "FROM excellent_achievement ea " +
            "INNER JOIN grade_summary gs ON ea.grade_summary_id = gs.summary_id AND gs.is_deleted = 0 " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON ea.student_id = su.user_id AND su.is_deleted = 0 " +
            "LEFT JOIN project_topic pt ON pt.topic_id = ps.topic_id AND pt.is_deleted = 0 " +
            "LEFT JOIN sys_user tu ON ps.teacher_id = tu.user_id AND tu.is_deleted = 0 " +
            "LEFT JOIN campus_threshold ct ON ct.campus_name = ps.campus_name " +
            "  AND ct.academic_year = ps.academic_year AND ct.is_deleted = 0 " +
            "WHERE ea.is_deleted = 0 AND ea.status = 'APPROVED' " +
            "AND (#{campusName} IS NULL OR #{campusName} = '' OR ps.campus_name = #{campusName}) " +
            "AND (#{academicYear} IS NULL OR #{academicYear} = '' OR ps.academic_year = #{academicYear}) " +
            "ORDER BY ps.campus_name, su.real_name")
    List<ExcellentAchievementResp> selectForExport(@Param("campusName") String campusName,
                                                    @Param("academicYear") String academicYear);

    // ==================== 查询可选认定的成绩列表（成绩总分 > 学校阈值） ====================

    @Select("SELECT gs.summary_id AS summaryId, su.real_name AS studentName, su.student_no AS studentNo, " +
            "ps.campus_name AS campusName, ps.academic_year AS academicYear, " +
            "gs.total_score AS gradeTotalScore, gs.final_grade_level AS gradeFinalGradeLevel, " +
            "ct.excellent_score AS campusThreshold " +
            "FROM grade_summary gs " +
            "INNER JOIN project_selection ps ON gs.selection_id = ps.selection_id AND ps.is_deleted = 0 " +
            "INNER JOIN sys_user su ON gs.student_id = su.user_id AND su.is_deleted = 0 " +
            "LEFT JOIN campus_threshold ct ON ct.campus_name = ps.campus_name " +
            "  AND ct.academic_year = ps.academic_year AND ct.is_deleted = 0 " +
            "WHERE gs.is_deleted = 0 " +
            "AND gs.total_score IS NOT NULL " +
            "AND ct.excellent_score IS NOT NULL " +
            "AND gs.total_score > ct.excellent_score " +
            "AND gs.summary_id NOT IN (SELECT grade_summary_id FROM excellent_achievement WHERE is_deleted = 0 AND status = 'APPROVED') " +
            "AND (#{campusName} IS NULL OR #{campusName} = '' OR ps.campus_name = #{campusName}) " +
            "ORDER BY gs.total_score DESC")
    List<GradeOptionResp> selectQualifiedGrades(@Param("campusName") String campusName);
}
