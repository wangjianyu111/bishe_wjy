package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.GradeInputReq;
import com.gdplatform.dto.GradeResp;

import java.math.BigDecimal;

public interface GradeService {

    /** 管理员分页查询 */
    Page<GradeResp> adminPage(long current, long size, String campusName, String academicYear,
                              String keyword, String gradeLevel);

    /** 教师分页查询（本校学生） */
    Page<GradeResp> teacherPage(long current, long size, String campusName, String academicYear,
                               String keyword, String gradeLevel);

    /** 学生分页查询（只看自己） */
    Page<GradeResp> studentPage(long current, long size);

    /** 教师录入/更新单次评分 */
    Long inputScore(GradeInputReq req);

    /** 管理员调整最终成绩 */
    void adjustScore(Long summaryId, BigDecimal adjustedTotalScore, String adjustedGradeLevel, String remark);

    /** 管理员锁定成绩（锁定后教师不能修改） */
    void lockScore(Long summaryId);

    /** 管理员解锁成绩 */
    void unlockScore(Long summaryId);

    /** 获取成绩详情（含所有教师评分明细） */
    GradeResp getDetail(Long summaryId);

    /** 删除单条教师评分记录 */
    void deleteRecord(Long gradeId);

    /** 导出成绩单（管理员） */
    void exportGrades(String campusName, String academicYear);

    /** 刷新汇总成绩（当教师提交评分时重新计算） */
    void refreshSummary(Long selectionId, Long studentId);
}
