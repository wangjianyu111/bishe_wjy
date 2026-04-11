package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;
import com.gdplatform.entity.SysUser;

import java.util.List;

public interface GuidanceRelationService {

    Page<GuidanceRelationResp> pageForAdmin(long current, long size, String campusName, String academicYear, String keyword);

    Page<GuidanceRelationResp> pageForTeacher(long current, long size, String academicYear, String keyword);

    Page<GuidanceRelationResp> pageForStudent(long current, long size, String academicYear);

    void add(GuidanceRelationReq req);

    void update(GuidanceRelationReq req);

    void delete(Long relationId);

    GuidanceRelationResp getDetail(Long relationId);

    List<GuidanceRelationResp> listByTeacher(Long teacherId, String academicYear);

    Long getTeacherIdByStudent(Long studentId, String academicYear);

    List<Long> getStudentIdsByTeacher(Long teacherId, String academicYear);

    boolean isStudentGuided(Long studentId, String academicYear);

    // ====== 教师仪表盘 ======
    // 教师：我的学生列表（分页）
    Page<GuidanceRelationPageResp> teacherStudentPage(long current, long size, String academicYear, String keyword);

    // 教师：我创建的答辩小组
    List<DefenseGroupCard> teacherCreatedGroups(String academicYear);

    // 教师：我加入的答辩小组
    List<DefenseGroupCard> teacherJoinedGroups(String academicYear);

    // ====== 学生仪表盘 ======
    // 学生：我的指导教师
    GuidanceRelationPageResp studentRelation(String academicYear);

    // 学生：我加入的答辩小组
    List<DefenseGroupCard> studentJoinedGroups(String academicYear);

    // ====== 申请流程 ======
    // 发送申请（双向）
    void sendApply(GuidanceRelationApplyReq req);

    // 获取我收到的申请
    Page<GuidanceRelationApplyResp> myReceivedApplies(long current, long size);

    // 获取我发出的申请
    Page<GuidanceRelationApplyResp> mySentApplies(long current, long size);

    // 处理申请（同意/拒绝）
    void handleApply(GuidanceRelationHandleReq req);

    // 取消我的申请
    void cancelApply(Long applyId);

    // 解除指导关系（带通知）
    void terminateRelation(Long relationId);
}
