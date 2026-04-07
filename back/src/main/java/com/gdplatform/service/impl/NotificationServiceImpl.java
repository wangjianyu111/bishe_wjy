package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.NotificationAddReq;
import com.gdplatform.dto.NotificationResp;
import com.gdplatform.dto.NotificationReadStatusResp;
import com.gdplatform.dto.NotificationUpdateReq;
import com.gdplatform.dto.ReceiverStatusResp;
import com.gdplatform.entity.SysNotification;
import com.gdplatform.mapper.SysNotificationMapper;
import com.gdplatform.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SysNotificationMapper sysNotificationMapper;

    @Override
    public Page<NotificationResp> pageNotifications(long current, long size, String keyword) {
        Page<NotificationResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<NotificationResp> records = sysNotificationMapper.selectNotificationPage(keyword, offset, size);
        long total = sysNotificationMapper.countNotificationPage(keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public NotificationResp getById(Long noticeId) {
        SysNotification n = sysNotificationMapper.selectById(noticeId);
        if (n == null) {
            throw new BizException("通知不存在");
        }
        NotificationResp resp = new NotificationResp();
        resp.setNoticeId(n.getNoticeId());
        resp.setTitle(n.getTitle());
        resp.setContent(n.getContent());
        resp.setNoticeType(n.getNoticeType());
        resp.setSenderId(n.getSenderId());
        resp.setReceiverId(n.getReceiverId());
        resp.setIsRead(n.getIsRead());
        resp.setBizType(n.getBizType());
        resp.setBizId(n.getBizId());
        resp.setCreateTime(n.getCreateTime());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(NotificationAddReq req, Long senderId) {
        if (req.getReceiverIds() == null || req.getReceiverIds().isEmpty()) {
            throw new BizException("请选择至少一个接收人");
        }
        String groupId = UUID.randomUUID().toString();
        for (Long receiverId : req.getReceiverIds()) {
            SysNotification n = new SysNotification();
            n.setGroupId(groupId);
            n.setTitle(req.getTitle());
            n.setContent(req.getContent());
            n.setNoticeType(req.getNoticeType());
            n.setSenderId(senderId);
            n.setReceiverId(receiverId);
            n.setIsRead(0);
            n.setBizType(req.getBizType());
            n.setBizId(req.getBizId());
            sysNotificationMapper.insert(n);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(NotificationUpdateReq req) {
        SysNotification n = sysNotificationMapper.selectById(req.getNoticeId());
        if (n == null) {
            throw new BizException("通知不存在");
        }
        if (req.getTitle() != null) {
            n.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            n.setContent(req.getContent());
        }
        if (req.getNoticeType() != null) {
            n.setNoticeType(req.getNoticeType());
        }
        sysNotificationMapper.updateById(n);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noticeId) {
        SysNotification n = sysNotificationMapper.selectById(noticeId);
        if (n == null) {
            throw new BizException("通知不存在");
        }
        if (n.getGroupId() != null) {
            sysNotificationMapper.delete(new QueryWrapper<SysNotification>().eq("group_id", n.getGroupId()));
        } else {
            sysNotificationMapper.deleteById(noticeId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long noticeId) {
        SysNotification n = new SysNotification();
        n.setNoticeId(noticeId);
        n.setIsRead(1);
        sysNotificationMapper.updateById(n);
    }

    @Override
    public List<NotificationResp> getListByReceiverId(Long receiverId) {
        return sysNotificationMapper.selectListByReceiverId(receiverId);
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        return sysNotificationMapper.countUnreadByReceiverId(receiverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long receiverId) {
        UpdateWrapper<SysNotification> wrapper = new UpdateWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.eq("is_read", 0);
        SysNotification n = new SysNotification();
        n.setIsRead(1);
        sysNotificationMapper.update(n, wrapper);
    }

    @Override
    public NotificationReadStatusResp getReadStatus(Long noticeId) {
        SysNotification representative = sysNotificationMapper.selectById(noticeId);
        if (representative == null) {
            throw new BizException("通知不存在");
        }
        String groupId = representative.getGroupId();
        QueryWrapper<SysNotification> qw = new QueryWrapper<>();
        if (groupId != null) {
            qw.eq("group_id", groupId);
        } else {
            qw.eq("notice_id", noticeId);
        }
        long total = sysNotificationMapper.selectCount(qw);

        qw.eq("is_read", 1);
        long read = sysNotificationMapper.selectCount(qw);

        NotificationReadStatusResp resp = new NotificationReadStatusResp();
        resp.setNoticeId(representative.getNoticeId());
        resp.setTitle(representative.getTitle());
        resp.setContent(representative.getContent());
        resp.setNoticeType(representative.getNoticeType());
        resp.setSenderId(representative.getSenderId());
        resp.setTotalCount((int) total);
        resp.setReadCount((int) read);
        resp.setUnreadCount((int) (total - read));
        resp.setCreateTime(representative.getCreateTime());
        return resp;
    }

    @Override
    public List<ReceiverStatusResp> getReceiverListByNoticeId(Long noticeId) {
        SysNotification representative = sysNotificationMapper.selectById(noticeId);
        if (representative == null) {
            throw new BizException("通知不存在");
        }
        if (representative.getGroupId() != null) {
            return sysNotificationMapper.selectReceiverStatusByGroupId(representative.getGroupId());
        }
        return sysNotificationMapper.selectReceiverStatusByNoticeId(noticeId);
    }
}
