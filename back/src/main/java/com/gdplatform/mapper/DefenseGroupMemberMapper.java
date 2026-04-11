package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.DefenseGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DefenseGroupMemberMapper extends BaseMapper<DefenseGroupMember> {

    void deleteByGroupId(@Param("groupId") Long groupId);

    List<DefenseGroupMember> selectTeachersByGroupId(@Param("groupId") Long groupId);

    List<DefenseGroupMember> selectStudentsByGroupId(@Param("groupId") Long groupId);
}
