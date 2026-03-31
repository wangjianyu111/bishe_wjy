package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.NotificationResp;
import com.gdplatform.dto.ReceiverStatusResp;
import com.gdplatform.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNotificationMapper extends BaseMapper<SysNotification> {

    List<NotificationResp> selectNotificationPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") long limit);

    long countNotificationPage(@Param("keyword") String keyword);

    List<NotificationResp> selectListByReceiverId(@Param("receiverId") Long receiverId);

    long countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    List<ReceiverStatusResp> selectReceiverStatusByNoticeId(@Param("noticeId") Long noticeId);

    List<ReceiverStatusResp> selectReceiverStatusByGroupId(@Param("groupId") String groupId);
}
