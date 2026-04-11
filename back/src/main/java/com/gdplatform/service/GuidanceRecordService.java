package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.GuidanceRecordReq;
import com.gdplatform.dto.GuidanceRecordResp;
import com.gdplatform.dto.GuidanceStatsResp;
import com.gdplatform.dto.SelectionForGuidanceResp;

import java.util.List;

public interface GuidanceRecordService {

    /** 教师：查询可添加指导的选题列表 */
    List<SelectionForGuidanceResp> getSelectionsForTeacher();

    /** 管理员：分页查询 */
    Page<GuidanceRecordResp> pageForAdmin(long current, long size, String campusName, String academicYear, String keyword, Long teacherId);

    /** 教师：分页查询（本校） */
    Page<GuidanceRecordResp> pageForTeacher(long current, long size, String academicYear, String keyword);

    /** 学生：分页查询（自己的记录） */
    Page<GuidanceRecordResp> pageForStudent(long current, long size, String academicYear);

    /** 教师：添加指导记录 */
    Long add(GuidanceRecordReq req);

    /** 教师：更新指导记录 */
    void update(GuidanceRecordReq req);

    /** 教师/管理员：删除记录 */
    void delete(Long guideId);

    /** 学生：填写反馈 */
    void addFeedback(Long guideId, String feedback);

    /** 获取详情 */
    GuidanceRecordResp getDetail(Long guideId);

    /** 获取统计信息 */
    GuidanceStatsResp getStats(Long studentId);
}
