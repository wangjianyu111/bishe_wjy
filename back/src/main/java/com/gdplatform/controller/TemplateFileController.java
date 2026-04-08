package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.TemplateAddReq;
import com.gdplatform.dto.TemplatePageReq;
import com.gdplatform.dto.TemplateResp;
import com.gdplatform.dto.TemplateUpdateReq;
import com.gdplatform.entity.SysUser;
import com.gdplatform.service.TemplateFileService;
import com.gdplatform.util.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/doc/template")
@RequiredArgsConstructor
public class TemplateFileController {

    private final TemplateFileService templateService;

    /**
     * 上传模板文件（管理员/教师）
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('doc:template:upload')")
    public R<TemplateResp> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute TemplateAddReq req) {
        TemplateResp resp = templateService.uploadTemplate(file, req);
        return R.ok(resp);
    }

    /**
     * 分页查询模板列表
     * - 教师/管理员：可查看所有学校的模板，支持按学校名称筛选
     * - 学生：自动过滤为本校模板，不显示学校筛选框
     */
    @GetMapping
    @PreAuthorize("hasAuthority('doc:template:list')")
    public R<Page<TemplateResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String campusName) {
        SysUser user = SecurityUtils.currentUser();
        // 学生用户自动过滤为本校模板，不受前端 campusName 参数影响
        if (user.getUserType() == 1) {
            campusName = user.getCampusName();
        }
        TemplatePageReq req = new TemplatePageReq();
        req.setPhase(phase);
        req.setCampusName(campusName);
        return R.ok(templateService.pageTemplate(req, current, size));
    }

    /**
     * 修改模板信息（阶段/学校，不改文件）
     */
    @PutMapping
    @PreAuthorize("hasAuthority('doc:template:edit')")
    public R<Void> updateTemplate(@Valid @RequestBody TemplateUpdateReq req) {
        templateService.updateTemplate(req);
        return R.ok();
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAuthority('doc:template:del')")
    public R<Void> deleteTemplate(@PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return R.ok();
    }

    /**
     * 下载模板文件
     * - 教师/管理员：可下载任意模板
     * - 学生：仅可下载本校模板
     */
    @GetMapping("/download/{templateId}")
    @PreAuthorize("hasAuthority('doc:template:download')")
    public void downloadTemplate(@PathVariable Long templateId, HttpServletResponse response) throws IOException {
        SysUser user = SecurityUtils.currentUser();
        if (user.getUserType() == 1) {
            templateService.downloadStudentTemplate(templateId, user.getCampusName(), response);
        } else {
            templateService.downloadTemplate(templateId, response);
        }
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{templateId}")
    @PreAuthorize("hasAuthority('doc:template:list')")
    public R<TemplateResp> getDetail(@PathVariable Long templateId) {
        return R.ok(templateService.getDetail(templateId));
    }
}
