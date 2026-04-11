package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.*;

import java.util.List;

public interface DocVersionService {
    void submit(VersionSubmitReq req);
    void deleteByStudent(Long versionId);
    Page<VersionResp> pageForAdmin(VersionPageReq req, long current, long size);
    Page<VersionResp> pageForTeacher(VersionPageReq req, long current, long size);
    List<VersionResp> listByStudent(Long selectionId);
    VersionResp getDetail(Long versionId);
}
