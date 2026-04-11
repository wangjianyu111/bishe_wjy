package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.AchievementPageReq;
import com.gdplatform.dto.AchievementResp;
import com.gdplatform.entity.AchievementSubmit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementSubmitMapper extends BaseMapper<AchievementSubmit> {

    @Select("SELECT * FROM achievement_submit WHERE submit_id = #{submitId} AND is_deleted = 0")
    AchievementSubmit selectBySubmitId(@Param("submitId") Long submitId);

    @Select("SELECT * FROM achievement_submit WHERE selection_id = #{selectionId} AND is_deleted = 0 LIMIT 1")
    AchievementSubmit selectBySelectionId(@Param("selectionId") Long selectionId);

    List<AchievementResp> selectAchievementPage(@Param("req") AchievementPageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countAchievementPage(@Param("req") AchievementPageReq req);

    List<AchievementResp> selectAchievementPageForTeacher(@Param("req") AchievementPageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countAchievementPageForTeacher(@Param("req") AchievementPageReq req);

    @Update("UPDATE achievement_submit SET is_deleted = 1 WHERE submit_id = #{submitId}")
    void deleteBySubmitId(@Param("submitId") Long submitId);
}
