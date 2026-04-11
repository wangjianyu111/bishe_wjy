package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.DefenseGroupReq;
import com.gdplatform.dto.DefenseGroupResp;
import com.gdplatform.entity.SysUser;

import java.util.List;

public interface DefenseGroupService {

    Page<DefenseGroupResp> pageForTeacher(long current, long size, String academicYear, String keyword);

    DefenseGroupResp create(DefenseGroupReq req);

    void update(DefenseGroupReq req);

    void delete(Long groupId);

    DefenseGroupResp getDetail(Long groupId);

    List<DefenseGroupResp> listByTeacher(Long teacherId, String academicYear);

    List<Long> getGroupIdsByTeacherId(Long teacherId, String academicYear);

    boolean isTeacherInGroup(Long teacherId, Long groupId);

    List<Long> getTeacherIdsInGroup(Long groupId);

    List<Long> getStudentIdsInGroup(Long groupId, Long studentId);

    List<Long> getStudentIdsInGroupByTeacher(Long teacherId, String academicYear);
}
