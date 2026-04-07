package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.TopicBankItemResp;
import com.gdplatform.dto.TopicResp;
import com.gdplatform.entity.ProjectTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProjectTopicMapper extends BaseMapper<ProjectTopic> {

    List<TopicResp> selectTopicPage(
            @Param("campusId") Long campusId,
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countTopicPage(
            @Param("campusId") Long campusId,
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("status") String status,
            @Param("keyword") String keyword);

    List<TopicResp> selectAllForExport(
            @Param("campusId") Long campusId,
            @Param("campusName") String campusName,
            @Param("academicYear") String academicYear,
            @Param("status") String status,
            @Param("keyword") String keyword);

    List<TopicBankItemResp> selectTopicBankByCampus(
            @Param("campusId") Long campusId,
            @Param("teacherId") Long teacherId,
            @Param("academicYear") String academicYear);

    @Update("UPDATE project_topic SET current_count = current_count + 1 WHERE topic_id = #{topicId} AND is_deleted = 0")
    void addCurrentCount(@Param("topicId") Long topicId);
}
