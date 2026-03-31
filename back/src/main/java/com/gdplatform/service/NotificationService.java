package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.NotificationAddReq;
import com.gdplatform.dto.NotificationResp;
import com.gdplatform.dto.NotificationReadStatusResp;
import com.gdplatform.dto.NotificationUpdateReq;
import com.gdplatform.dto.ReceiverStatusResp;
import com.gdplatform.dto.ReceiverStatusResp;

import java.util.List;

public interface NotificationService {

    Page<NotificationResp> pageNotifications(long current, long size, String keyword);

    NotificationResp getById(Long noticeId);

    void add(NotificationAddReq req, Long senderId);

    void update(NotificationUpdateReq req);

    void delete(Long noticeId);

    void markRead(Long noticeId);

    List<NotificationResp> getListByReceiverId(Long receiverId);

    long getUnreadCount(Long receiverId);

    void markAllRead(Long receiverId);

    NotificationReadStatusResp getReadStatus(Long noticeId);

    List<ReceiverStatusResp> getReceiverListByNoticeId(Long noticeId);
}
