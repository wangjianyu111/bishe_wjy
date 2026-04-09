package com.gdplatform.mapper;

import com.gdplatform.dto.ProposalPageReq;
import com.gdplatform.dto.ProposalResp;
import com.gdplatform.entity.ProjectProposal;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectProposalMapper {

    ProjectProposal selectById(@Param("proposalId") Long proposalId);

    void insert(ProjectProposal proposal);

    void updateById(ProjectProposal proposal);

    void deleteById(@Param("proposalId") Long proposalId);

    ProposalResp selectBySelectionId(@Param("selectionId") Long selectionId);

    ProposalResp selectLatestByStudentId(@Param("studentId") Long studentId);

    List<ProposalResp> selectProposalPage(@Param("req") ProposalPageReq req,
                                          @Param("offset") long offset,
                                          @Param("limit") long limit);

    long countProposalPage(@Param("req") ProposalPageReq req);

    List<ProposalResp> selectProposalPageForTeacher(@Param("req") ProposalPageReq req,
                                                    @Param("offset") long offset,
                                                    @Param("limit") long limit);

    long countProposalPageForTeacher(@Param("req") ProposalPageReq req);

    List<ProposalResp> selectAllByStudentId(@Param("studentId") Long studentId);
}
