package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.DefenseArrangeResp;
import com.gdplatform.dto.DefenseSessionReq;
import com.gdplatform.dto.DefenseSessionResp;

import java.util.List;

public interface DefenseService {

    void publish(DefenseSessionReq req);

    Page<DefenseSessionResp> pageForAdmin(long current, long size, String campusName, String academicYear, String keyword);

    Page<DefenseSessionResp> pageForTeacher(long current, long size, String academicYear, String keyword);

    Page<DefenseSessionResp> pageForStudent(long current, long size, String academicYear);

    Page<DefenseArrangeResp> arrangementPageForAdmin(long current, long size, Long sessionId, String keyword);

    DefenseSessionResp getSessionDetail(Long sessionId);
}
