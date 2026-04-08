package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

import java.util.List;

public interface ProjectProgressService {

    void addProgress(ProgressAddReq req);

    void updateProgress(ProgressUpdateReq req);

    void deleteProgress(Long progressId);

    List<ProgressResp> getProgressListBySelection(Long selectionId);

    Page<ProgressResp> pageProgressForAdmin(ProgressPageReq req, long current, long size);

    Page<ProgressResp> pageProgressForTeacher(ProgressPageReq req, long current, long size);

    Page<ProgressResp> pageProgressForStudent(ProgressPageReq req, long current, long size);

    ProgressResp getProgressDetail(Long progressId);
}
