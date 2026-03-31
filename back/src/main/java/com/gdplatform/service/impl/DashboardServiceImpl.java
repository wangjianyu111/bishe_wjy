package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gdplatform.dto.DashboardOverviewResp;
import com.gdplatform.dto.MonthCountPoint;
import com.gdplatform.dto.NameValue;
import com.gdplatform.dto.RecentTopicRow;
import com.gdplatform.entity.ProjectSelection;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.DashboardMapper;
import com.gdplatform.mapper.ProjectSelectionMapper;
import com.gdplatform.mapper.ProjectTopicMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.DashboardService;
import com.gdplatform.service.NotificationService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private final SysUserMapper sysUserMapper;
    private final ProjectTopicMapper projectTopicMapper;
    private final ProjectSelectionMapper projectSelectionMapper;
    private final DashboardMapper dashboardMapper;
    private final NotificationService notificationService;

    @Override
    public DashboardOverviewResp overview() {
        Long uid = SecurityUtils.currentUser().getUserId();
        long totalUsers = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery());
        long totalTopics = projectTopicMapper.selectCount(Wrappers.<ProjectTopic>lambdaQuery());
        long totalSelections = projectSelectionMapper.selectCount(Wrappers.<ProjectSelection>lambdaQuery());
        long unread = notificationService.getUnreadCount(uid);

        List<NameValue> statusDist = dashboardMapper.selectTopicStatusDistribution();
        List<MonthCountPoint> topicMonthly = fillLast12Months(dashboardMapper.selectTopicMonthlyNewLast12());
        List<MonthCountPoint> selectionMonthly = fillLast12Months(dashboardMapper.selectSelectionMonthlyLast12());
        List<RecentTopicRow> recent = dashboardMapper.selectRecentTopics(6);

        return DashboardOverviewResp.builder()
                .totalUsers(totalUsers)
                .totalTopics(totalTopics)
                .totalSelections(totalSelections)
                .unreadNotifications(unread)
                .topicStatusDistribution(statusDist)
                .topicMonthlyNew(topicMonthly)
                .selectionMonthly(selectionMonthly)
                .recentTopics(recent)
                .build();
    }

    /**
     * 补齐最近 12 个月（含当月），缺失月份补 0，便于前端折线/柱状图连续展示。
     */
    private List<MonthCountPoint> fillLast12Months(List<MonthCountPoint> raw) {
        Map<String, Long> map = new HashMap<>();
        if (raw != null) {
            for (MonthCountPoint p : raw) {
                if (p.getMonth() != null) {
                    map.put(p.getMonth(), p.getTotal() != null ? p.getTotal() : 0L);
                }
            }
        }
        List<MonthCountPoint> out = new ArrayList<>(12);
        LocalDate end = LocalDate.now().withDayOfMonth(1);
        for (int i = 11; i >= 0; i--) {
            LocalDate d = end.minusMonths(i);
            String key = d.format(YM);
            out.add(new MonthCountPoint(key, map.getOrDefault(key, 0L)));
        }
        return out;
    }
}
