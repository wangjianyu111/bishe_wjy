package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import com.gdplatform.entity.ProjectSelection;

import java.util.List;

public interface ProjectSelectionService {
    // 学生端：选题申请
    void apply(SelectionApplyReq req);

    // 管理员/教师端：分页查询选题申请列表
    Page<SelectionResp> pageSelections(SelectionPageReq req, long current, long size);

    // 学生端：查看自己的选题申请记录
    SelectionResp getMySelection();

    // 管理员/教师：审批通过
    void approve(SelectionActionReq req);

    // 管理员/教师：审批驳回
    void reject(SelectionActionReq req);

    // 学生：撤回申请
    void withdraw(Long selectionId);

    // 查询某个学校的所有教师（带工号）
    List<TeacherResp> listTeachersByCampus(Long campusId);

    // 根据学校名称查询教师（模糊匹配）
    List<TeacherResp> listTeachersByCampusName(String campusName);

    // 查询某个学校的题目库题目
    List<TopicBankItemResp> listTopicBankByCampus(Long campusId, Long teacherId, String academicYear);

    // 查询所有校区（带是否有题目库标记）
    List<CampusResp> listCampuses();
}
