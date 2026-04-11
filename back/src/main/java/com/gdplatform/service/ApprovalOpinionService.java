package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.ApprovalOpinionReq;
import com.gdplatform.dto.ApprovalOpinionResp;

public interface ApprovalOpinionService {

    /** 管理员分页查询 */
    Page<ApprovalOpinionResp> adminPage(long current, long size, String campusName, String academicYear,
                                        String keyword, String status, String docType);

    /** 教师分页查询 */
    Page<ApprovalOpinionResp> teacherPage(long current, long size, String academicYear,
                                           String keyword, String status, String docType);

    /** 学生查看自己的审批意见 */
    Page<ApprovalOpinionResp> studentPage(long current, long size, String academicYear,
                                           String status, String docType);

    /** 教师提交/更新审批意见 */
    Long submit(ApprovalOpinionReq req);

    /** 教师更新审批意见状态（通过/驳回） */
    void updateStatus(ApprovalOpinionReq req);

    /** 获取审批意见详情 */
    ApprovalOpinionResp getDetail(Long opinionId);

    /** 删除审批意见 */
    void delete(Long opinionId);
}
