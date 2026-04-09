package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import java.util.List;

public interface ProjectProposalService {

    void submit(ProposalSubmitReq req);

    void recall(Long proposalId);

    ProposalResp getMyProposal();

    List<ProposalResp> getMyProposalList();

    Page<ProposalResp> pageForAdmin(ProposalPageReq req, long current, long size);

    Page<ProposalResp> pageForTeacher(ProposalPageReq req, long current, long size);

    void review(ProposalReviewReq req);

    ProposalResp getDetail(Long proposalId);
}
