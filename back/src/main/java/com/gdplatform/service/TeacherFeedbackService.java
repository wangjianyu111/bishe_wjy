package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

public interface TeacherFeedbackService {

    void submit(TeacherFeedbackReq req);

    Page<TeacherFeedbackResp> pageForAdmin(long current, long size,
            String campusName, String academicYear, String feedbackType, String status, String keyword);

    Page<TeacherFeedbackResp> pageForTeacher(long current, long size,
            String academicYear, String feedbackType, String status);

    void handle(TeacherFeedbackHandleReq req);

    FeedbackStatsResp statsForAdmin(String campusName, String academicYear);

    FeedbackStatsResp statsForTeacher(String academicYear);
}
