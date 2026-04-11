package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.DefenseGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DefenseGroupMapper extends BaseMapper<DefenseGroup> {

    List<Long> selectTeacherIdsByGroupId(@Param("groupId") Long groupId);

    List<Long> selectStudentIdsByGroupId(@Param("groupId") Long groupId);

    List<Long> selectGroupIdsByTeacherId(@Param("teacherId") Long teacherId, @Param("academicYear") String academicYear);

    List<Long> selectStudentIdsByGroupIdAndStudent(@Param("groupId") Long groupId, @Param("studentId") Long studentId);

    List<Long> selectGroupIdsByLeader(@Param("leaderId") Long leaderId);
}
