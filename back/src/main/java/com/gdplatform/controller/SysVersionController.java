package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.VersionAddReq;
import com.gdplatform.dto.VersionReq;
import com.gdplatform.dto.VersionResp;
import com.gdplatform.dto.VersionRolloutReq;
import com.gdplatform.service.SysVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/version")
@RequiredArgsConstructor
public class SysVersionController {

    private final SysVersionService versionService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sysops:version')")
    public R<Page<VersionResp>> page(VersionReq req) {
        return R.ok(versionService.pageVersions(req));
    }

    @GetMapping("/{versionId}")
    @PreAuthorize("hasAuthority('sysops:version')")
    public R<VersionResp> getDetail(@PathVariable Long versionId) {
        return R.ok(versionService.getVersionDetail(versionId));
    }

    @GetMapping("/current")
    public R<VersionResp> getCurrentVersion(@RequestParam String appType) {
        return R.ok(versionService.getCurrentVersion(appType));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sysops:version')")
    public R<Void> addVersion(@Valid @RequestBody VersionAddReq req) {
        versionService.addVersion(req);
        return R.ok();
    }

    @PostMapping("/rollout")
    @PreAuthorize("hasAuthority('sysops:version')")
    public R<Void> performRollout(@Valid @RequestBody VersionRolloutReq req) {
        versionService.performRollout(req);
        return R.ok();
    }

    @DeleteMapping("/{versionId}")
    @PreAuthorize("hasAuthority('sysops:version')")
    public R<Void> deleteVersion(@PathVariable Long versionId) {
        versionService.deleteVersion(versionId);
        return R.ok();
    }
}
