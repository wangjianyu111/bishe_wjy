package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.CampusThreshold;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CampusThresholdMapper extends BaseMapper<CampusThreshold> {

    @Select("SELECT * FROM campus_threshold WHERE is_deleted = 0 " +
            "AND (#{campusName} IS NULL OR #{campusName} = '' OR campus_name = #{campusName}) " +
            "AND (#{academicYear} IS NULL OR #{academicYear} = '' OR academic_year = #{academicYear}) " +
            "ORDER BY campus_name, academic_year DESC")
    List<CampusThreshold> selectListByCondition(@Param("campusName") String campusName,
                                               @Param("academicYear") String academicYear);
}
