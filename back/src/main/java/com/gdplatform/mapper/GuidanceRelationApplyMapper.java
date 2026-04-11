package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.GuidanceRelationApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GuidanceRelationApplyMapper extends BaseMapper<GuidanceRelationApply> {

    Long selectPendingApplyId(@Param("fromUserId") Long fromUserId,
                             @Param("toUserId") Long toUserId,
                             @Param("academicYear") String academicYear);

    List<Long> selectPendingApplyIds(@Param("toUserId") Long toUserId);
}
