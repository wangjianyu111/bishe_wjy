package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

import java.util.List;

public interface DocArchiveService {
    void submit(ArchiveSubmitReq req);
    void recall(Long archiveId);
    Page<ArchiveResp> pageForAdmin(ArchivePageReq req, long current, long size);
    Page<ArchiveResp> pageForTeacher(ArchivePageReq req, long current, long size);
    List<ArchiveResp> listByStudent(Long selectionId);
    ArchiveResp getDetail(Long archiveId);
    void review(ArchiveReviewReq req);
    void checkSubmissionAllowed(Long selectionId, String stageName);
}
