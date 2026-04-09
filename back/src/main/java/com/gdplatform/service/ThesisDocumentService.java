package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

import java.util.List;

public interface ThesisDocumentService {
    void submit(ThesisSubmitReq req);
    void recall(Long thesisId);
    ThesisResp getMyThesis();
    List<ThesisResp> getMyThesisList();
    Page<ThesisResp> pageForAdmin(ThesisPageReq req, long current, long size);
    Page<ThesisResp> pageForTeacher(ThesisPageReq req, long current, long size);
    void review(ThesisReviewReq req);
    ThesisResp getDetail(Long thesisId);
}
