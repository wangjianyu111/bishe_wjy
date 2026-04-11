package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

import java.util.List;

public interface AchievementSubmitService {
    Long submit(AchievementSubmitReq req);
    void deleteByStudent(Long submitId);
    Page<AchievementResp> pageForAdmin(AchievementPageReq req, long current, long size);
    Page<AchievementResp> pageForTeacher(AchievementPageReq req, long current, long size);
    List<AchievementResp> listByStudent(Long selectionId);
    AchievementResp getDetail(Long submitId);
    AchievementResp getMySubmission(Long selectionId);
}
