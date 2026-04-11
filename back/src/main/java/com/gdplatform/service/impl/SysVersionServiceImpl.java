package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.VersionAddReq;
import com.gdplatform.dto.VersionReq;
import com.gdplatform.dto.VersionResp;
import com.gdplatform.dto.VersionRolloutReq;
import com.gdplatform.entity.SysVersion;
import com.gdplatform.entity.SysVersionRollout;
import com.gdplatform.mapper.SysVersionMapper;
import com.gdplatform.mapper.SysVersionRolloutMapper;
import com.gdplatform.service.SysVersionService;
import com.gdplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysVersionServiceImpl implements SysVersionService {

    private final SysVersionMapper versionMapper;
    private final SysVersionRolloutMapper rolloutMapper;

    @Override
    public Page<VersionResp> pageVersions(VersionReq req) {
        Page<VersionResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<VersionResp> result = versionMapper.selectVersionPage(page, req);
        for (VersionResp r : result.getRecords()) {
            enrichLabels(r);
        }
        return result;
    }

    @Override
    public VersionResp getVersionDetail(Long versionId) {
        SysVersion v = versionMapper.selectById(versionId);
        if (v == null) {
            throw new RuntimeException("版本不存在");
        }
        VersionResp resp = convertToResp(v);
        enrichLabels(resp);
        return resp;
    }

    @Override
    @Transactional
    public void addVersion(VersionAddReq req) {
        SysVersion v = new SysVersion();
        v.setVersionCode(req.getVersionCode());
        v.setVersionName(req.getVersionName());
        v.setAppType(req.getAppType());
        v.setStatus("DEVELOPING");
        v.setReleaseType(req.getReleaseType());
        v.setRolloutPercent(req.getRolloutPercent() != null ? req.getRolloutPercent() : 0);
        v.setChangelog(req.getChangelog());
        v.setDownloadUrl(req.getDownloadUrl());
        v.setMinCompatibleVersion(req.getMinCompatibleVersion());
        v.setFeatureList(req.getFeatureList());
        v.setForceUpdate(req.getForceUpdate() != null ? req.getForceUpdate() : 0);

        var user = SecurityUtils.currentUser();
        v.setDeployedBy(user.getUserId());
        v.setDeployedByName(user.getRealName());
        v.setDeployedTime(LocalDateTime.now());

        versionMapper.insert(v);
    }

    @Override
    @Transactional
    public void performRollout(VersionRolloutReq req) {
        SysVersion v = versionMapper.selectById(req.getVersionId());
        if (v == null) {
            throw new RuntimeException("版本不存在");
        }

        var user = SecurityUtils.currentUser();

        if ("GRAY".equals(req.getRolloutType())) {
            v.setStatus("RELEASED");
            v.setRolloutPercent(req.getRolloutPercent());
        } else if ("FULL".equals(req.getRolloutType())) {
            // 先将同类型版本全部设为 DEPRECATED
            versionMapper.update(null, Wrappers.<SysVersion>lambdaUpdate()
                    .eq(SysVersion::getAppType, v.getAppType())
                    .ne(SysVersion::getVersionId, v.getVersionId())
                    .eq(SysVersion::getStatus, "RELEASED")
                    .set(SysVersion::getStatus, "DEPRECATED"));

            v.setStatus("RELEASED");
            v.setRolloutPercent(100);
        } else if ("ROLLBACK".equals(req.getRolloutType())) {
            // 找到上一个稳定版本
            List<SysVersion> stableVersions = versionMapper.selectList(
                    Wrappers.<SysVersion>lambdaQuery()
                            .eq(SysVersion::getAppType, v.getAppType())
                            .in(SysVersion::getStatus, "RELEASED", "ROLLBACKED")
                            .ne(SysVersion::getVersionId, v.getVersionId())
                            .orderByDesc(SysVersion::getDeployedTime)
                            .last("LIMIT 1"));
            if (stableVersions.isEmpty()) {
                throw new RuntimeException("没有可回滚的版本");
            }
            SysVersion prevVersion = stableVersions.get(0);

            prevVersion.setStatus("RELEASED");
            prevVersion.setRolloutPercent(100);
            versionMapper.updateById(prevVersion);

            v.setStatus("ROLLBACKED");
            v.setRolloutPercent(0);
        }

        versionMapper.updateById(v);

        // 记录操作日志
        SysVersionRollout rollout = new SysVersionRollout();
        rollout.setVersionId(v.getVersionId());
        rollout.setRolloutType(req.getRolloutType());
        rollout.setRolloutPercent(req.getRolloutPercent() != null ? req.getRolloutPercent() : 100);
        rollout.setRolloutStatus("COMPLETED");
        rollout.setRolloutTime(LocalDateTime.now());
        rollout.setRolloutBy(user.getUserId());
        rollout.setRolloutByName(user.getRealName());
        rollout.setRemark(req.getRemark());
        rollout.setCreateTime(LocalDateTime.now());
        rolloutMapper.insert(rollout);
    }

    @Override
    public void deleteVersion(Long versionId) {
        versionMapper.deleteById(versionId);
    }

    @Override
    public VersionResp getCurrentVersion(String appType) {
        SysVersion v = versionMapper.selectOne(
                Wrappers.<SysVersion>lambdaQuery()
                        .eq(SysVersion::getAppType, appType)
                        .eq(SysVersion::getStatus, "RELEASED")
                        .orderByDesc(SysVersion::getDeployedTime)
                        .last("LIMIT 1"));
        if (v == null) {
            return null;
        }
        VersionResp resp = convertToResp(v);
        enrichLabels(resp);
        return resp;
    }

    private VersionResp convertToResp(SysVersion v) {
        VersionResp resp = new VersionResp();
        resp.setVersionId(v.getVersionId());
        resp.setVersionCode(v.getVersionCode());
        resp.setVersionName(v.getVersionName());
        resp.setAppType(v.getAppType());
        resp.setStatus(v.getStatus());
        resp.setReleaseType(v.getReleaseType());
        resp.setRolloutPercent(v.getRolloutPercent());
        resp.setChangelog(v.getChangelog());
        resp.setDownloadUrl(v.getDownloadUrl());
        resp.setMinCompatibleVersion(v.getMinCompatibleVersion());
        resp.setFeatureList(v.getFeatureList());
        resp.setDeployedBy(v.getDeployedBy());
        resp.setDeployedByName(v.getDeployedByName());
        resp.setDeployedTime(v.getDeployedTime());
        resp.setForceUpdate(v.getForceUpdate());
        resp.setCreateTime(v.getCreateTime());
        return resp;
    }

    private void enrichLabels(VersionResp r) {
        r.setAppTypeLabel(switch (r.getAppType()) {
            case "FRONTEND" -> "后台前端";
            case "BACKEND" -> "后端服务";
            case "APP" -> "移动端";
            default -> r.getAppType();
        });
        r.setStatusLabel(switch (r.getStatus()) {
            case "DEVELOPING" -> "开发中";
            case "RELEASED" -> "已发布";
            case "ROLLBACKED" -> "已回滚";
            case "DEPRECATED" -> "已废弃";
            default -> r.getStatus();
        });
        r.setReleaseTypeLabel(switch (r.getReleaseType()) {
            case "STABLE" -> "正式版";
            case "BETA" -> "测试版";
            case "CANARY" -> "灰度版";
            default -> r.getReleaseType();
        });
    }
}
