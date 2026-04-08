package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.TemplateAddReq;
import com.gdplatform.dto.TemplatePageReq;
import com.gdplatform.dto.TemplateResp;
import com.gdplatform.dto.TemplateUpdateReq;
import com.gdplatform.entity.SysUser;
import com.gdplatform.entity.TemplateFile;
import com.gdplatform.mapper.TemplateFileMapper;
import com.gdplatform.service.TemplateFileService;
import com.gdplatform.util.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFileServiceImpl implements TemplateFileService {

    private final TemplateFileMapper templateMapper;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "txt", "doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "zip", "rar"
    );

    @Value("${gd.template.path:./uploads/templates}")
    private String templatePath;

    private File getTemplateDir() {
        Path base = Paths.get(templatePath).toAbsolutePath().normalize();
        File dir = base.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateResp uploadTemplate(MultipartFile file, TemplateAddReq req) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请上传文件");
        }

        String originalName = file.getOriginalFilename();
        String ext = getFileExtension(originalName).toLowerCase();

        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BizException("不支持的文件格式，仅支持：txt/doc/docx/pdf/xls/xlsx/ppt/pptx/zip/rar");
        }

        long maxSize = 50 * 1024 * 1024L; // 50MB
        if (file.getSize() > maxSize) {
            throw new BizException("文件大小不能超过 50MB");
        }

        SysUser user = SecurityUtils.currentUser();

        // 生成唯一文件名并保存
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dir = getTemplateDir();
        File dateDir = new File(dir, datePath);
        if (!dateDir.exists()) {
            dateDir.mkdirs();
        }
        Path targetPath = dateDir.toPath().resolve(fileName);
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BizException("文件保存失败：" + e.getMessage());
        }

        TemplateFile template = new TemplateFile();
        template.setPhase(req.getPhase());
        template.setCampusName(req.getCampusName());
        template.setFileName(fileName);
        template.setOriginalName(originalName);
        template.setFilePath(targetPath.toString());
        template.setFileSize(file.getSize());
        template.setFileType(ext);
        template.setUploaderId(user.getUserId());
        template.setUploaderName(user.getRealName());
        template.setUploadTime(LocalDateTime.now());

        templateMapper.insertTemplate(template);
        return toResp(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(TemplateUpdateReq req) {
        TemplateFile template = new TemplateFile();
        template.setTemplateId(req.getTemplateId());
        if (req.getPhase() != null && !req.getPhase().isBlank()) {
            template.setPhase(req.getPhase());
        }
        if (req.getCampusName() != null && !req.getCampusName().isBlank()) {
            template.setCampusName(req.getCampusName());
        }

        templateMapper.updateTemplate(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long templateId) {
        TemplateResp existing = templateMapper.selectRespById(templateId);
        if (existing == null) {
            throw new BizException("模板不存在");
        }
        templateMapper.deleteById(templateId);
    }

    @Override
    public Page<TemplateResp> pageTemplate(TemplatePageReq req, long current, long size) {
        Page<TemplateResp> page = new Page<>(current, size);
        List<TemplateResp> records = templateMapper.selectTemplatePage(req, (current - 1) * size, size);
        long total = templateMapper.countTemplatePage(req);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public TemplateResp getDetail(Long templateId) {
        TemplateResp resp = templateMapper.selectRespById(templateId);
        if (resp == null) {
            throw new BizException("模板不存在");
        }
        return resp;
    }

    @Override
    public void downloadTemplate(Long templateId, HttpServletResponse response) throws IOException {
        TemplateFile template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("模板不存在");
        }

        java.io.File file = new java.io.File(template.getFilePath());
        if (!file.exists()) {
            throw new BizException("文件已被删除或不存在");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + java.net.URLEncoder.encode(template.getOriginalName(), "UTF-8") + "\"");
        response.setContentLengthLong(template.getFileSize());

        try (java.io.InputStream is = new java.io.FileInputStream(file);
             java.io.OutputStream os = response.getOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
        }
    }

    private TemplateResp toResp(TemplateFile template) {
        TemplateResp resp = new TemplateResp();
        resp.setTemplateId(template.getTemplateId());
        resp.setPhase(template.getPhase());
        resp.setCampusName(template.getCampusName());
        resp.setFileName(template.getFileName());
        resp.setOriginalName(template.getOriginalName());
        resp.setFileSize(template.getFileSize());
        resp.setFileType(template.getFileType());
        resp.setUploaderId(template.getUploaderId());
        resp.setUploaderName(template.getUploaderName());
        resp.setUploadTime(template.getUploadTime());
        return resp;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
