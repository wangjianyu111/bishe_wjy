package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.CampusResp;
import com.gdplatform.entity.SysCampus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusMapper extends BaseMapper<SysCampus> {

    List<CampusResp> selectCampusPage(@Param("keyword") String keyword, @Param("offset") long offset, @Param("limit") long limit);

    long countCampusPage(@Param("keyword") String keyword);
}
