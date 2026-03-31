package com.gdplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardOverviewResp {

    private Long totalUsers;

    private Long totalTopics;

    private Long totalSelections;

    private Long unreadNotifications;

    private List<NameValue> topicStatusDistribution;

    private List<MonthCountPoint> topicMonthlyNew;

    private List<MonthCountPoint> selectionMonthly;

    private List<RecentTopicRow> recentTopics;
}
