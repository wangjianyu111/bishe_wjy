package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.NotificationAddReq;
import com.gdplatform.dto.NotificationResp;
import com.gdplatform.dto.NotificationReadStatusResp;
import com.gdplatform.dto.ReceiverStatusResp;
import com.gdplatform.dto.NotificationUpdateReq;
import com.gdplatform.service.NotificationService;
import com.gdplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/page")
    public R<Page<NotificationResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return R.ok(notificationService.pageNotifications(current, size, keyword));
    }

    @GetMapping("/{noticeId}")
    public R<NotificationResp> getById(@PathVariable Long noticeId) {
        return R.ok(notificationService.getById(noticeId));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody NotificationAddReq req) {
        Long senderId = SecurityUtils.currentUser().getUserId();
        notificationService.add(req, senderId);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody NotificationUpdateReq req) {
        notificationService.update(req);
        return R.ok();
    }

    @DeleteMapping("/{noticeId}")
    public R<Void> delete(@PathVariable Long noticeId) {
        notificationService.delete(noticeId);
        return R.ok();
    }

    @PutMapping("/read/{noticeId}")
    public R<Void> markRead(@PathVariable Long noticeId) {
        notificationService.markRead(noticeId);
        return R.ok();
    }

    @GetMapping("/user/list")
    public R<List<NotificationResp>> getListByReceiverId() {
        Long receiverId = SecurityUtils.currentUser().getUserId();
        return R.ok(notificationService.getListByReceiverId(receiverId));
    }

    @GetMapping("/unread-count")
    public R<Long> getUnreadCount() {
        Long receiverId = SecurityUtils.currentUser().getUserId();
        return R.ok(notificationService.getUnreadCount(receiverId));
    }

    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        Long receiverId = SecurityUtils.currentUser().getUserId();
        notificationService.markAllRead(receiverId);
        return R.ok();
    }

    @GetMapping("/read-status/{noticeId}")
    public R<NotificationReadStatusResp> getReadStatus(@PathVariable Long noticeId) {
        return R.ok(notificationService.getReadStatus(noticeId));
    }

    @GetMapping("/receivers/{noticeId}")
    public R<List<ReceiverStatusResp>> getReceivers(@PathVariable Long noticeId) {
        return R.ok(notificationService.getReceiverListByNoticeId(noticeId));
    }
}
