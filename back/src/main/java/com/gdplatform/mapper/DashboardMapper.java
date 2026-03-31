package com.gdplatform.mapper;

import com.gdplatform.dto.MonthCountPoint;
import com.gdplatform.dto.NameValue;
import com.gdplatform.dto.RecentTopicRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

    List<NameValue> selectTopicStatusDistribution();

    List<MonthCountPoint> selectTopicMonthlyNewLast12();

    List<MonthCountPoint> selectSelectionMonthlyLast12();

    List<RecentTopicRow> selectRecentTopics(@Param("limit") int limit);
}
