package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.DefenseArrangement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DefenseArrangementMapper extends BaseMapper<DefenseArrangement> {

    @Select("SELECT * FROM defense_arrangement WHERE arrange_id = #{arrangeId} AND is_deleted = 0")
    DefenseArrangement selectByArrangeId(@Param("arrangeId") Long arrangeId);

    @Select("SELECT student_id FROM defense_arrangement WHERE session_id = #{sessionId} AND is_deleted = 0")
    List<Long> selectStudentIdsBySessionId(@Param("sessionId") Long sessionId);
}
