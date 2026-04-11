package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.CampusThresholdReq;
import com.gdplatform.dto.ExcellentAchievementReq;
import com.gdplatform.dto.ExcellentAchievementResp;
import com.gdplatform.dto.GradeOptionResp;
import com.gdplatform.entity.CampusThreshold;

import java.util.List;

public interface ExcellentAchievementService {

    // ==================== 优秀成果列表 ====================

    /** 管理员分页查询优秀成果列表 */
    Page<ExcellentAchievementResp> adminPage(long current, long size,
                                            String campusName, String academicYear, String keyword);

    /** 教师分页查询（本校） */
    Page<ExcellentAchievementResp> teacherPage(long current, long size,
                                               String campusName, String academicYear, String keyword);

    /** 学生分页查询（自己的） */
    Page<ExcellentAchievementResp> studentPage(long current, long size);

    /** 学生自助查询（本校名单，按姓名/学号模糊搜索） */
    Page<ExcellentAchievementResp> studentSelfPage(long current, long size, String keyword);

    // ==================== 认定操作 ====================

    /** 认定优秀成果（成绩必须 > 学校设置的阈值） */
    void approve(ExcellentAchievementReq req);

    /** 撤销认定 */
    void revoke(Long excellentId);

    // ==================== 阈值管理 ====================

    /** 查询所有阈值配置 */
    List<CampusThreshold> listThresholds(String campusName, String academicYear);

    /** 新增或更新阈值配置 */
    void saveThreshold(CampusThresholdReq req);

    /** 删除阈值配置 */
    void deleteThreshold(Long thresholdId);

    // ==================== 候选成绩查询 ====================

    /** 查询可认定候选成绩（成绩总分 > 学校阈值，且未认定） */
    List<GradeOptionResp> listQualifiedGrades(String campusName);

    // ==================== Excel导出 ====================

    /** 获取导出数据 */
    List<ExcellentAchievementResp> getExportData(String campusName, String academicYear);

    /** 导出 Excel */
    void exportExcel(String campusName, String academicYear);
}
