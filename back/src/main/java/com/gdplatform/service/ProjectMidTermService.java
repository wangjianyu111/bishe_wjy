package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import java.util.List;

public interface ProjectMidTermService {

    void submit(MidTermSubmitReq req);

    void recall(Long midId);

    MidTermResp getMyMidTerm();

    List<MidTermResp> getMyMidTermList();

    Page<MidTermResp> pageForAdmin(MidTermPageReq req, long current, long size);

    Page<MidTermResp> pageForTeacher(MidTermPageReq req, long current, long size);

    void review(MidTermReviewReq req);

    MidTermResp getDetail(Long midId);
}
