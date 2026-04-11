package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.VersionAddReq;
import com.gdplatform.dto.VersionReq;
import com.gdplatform.dto.VersionResp;
import com.gdplatform.dto.VersionRolloutReq;

public interface SysVersionService {

    Page<VersionResp> pageVersions(VersionReq req);

    VersionResp getVersionDetail(Long versionId);

    void addVersion(VersionAddReq req);

    void performRollout(VersionRolloutReq req);

    void deleteVersion(Long versionId);

    VersionResp getCurrentVersion(String appType);
}
