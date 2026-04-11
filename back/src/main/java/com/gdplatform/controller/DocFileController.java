package com.gdplatform.controller;

import com.gdplatform.common.R;
import com.gdplatform.entity.DocFile;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.service.DocFileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/doc/file")
@RequiredArgsConstructor
public class DocFileController {

    private final DocFileMapper docFileMapper;
    private final DocFileService docFileService;

    /**
     * 预览/下载文件（所有已登录用户均可访问）
     */
    @GetMapping("/download/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        DocFile file = docFileMapper.selectByFileId(fileId);
        if (file == null || file.getIsDeleted() == 1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path path = Paths.get(file.getFilePath());
        if (!Files.exists(path)) {
            log.warn("文件物理路径不存在: {}", file.getFilePath());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            String mimeType = file.getMimeType();
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition",
                    "inline; filename=\"" + new String(file.getOriginalName().getBytes("UTF-8"), "ISO-8859-1") + "\"");
            response.setContentLengthLong(file.getFileSize());
            Files.copy(path, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("文件下载失败，fileId={}", fileId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除文件（管理员权限，物理删除 + 逻辑删除）
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasAuthority('doc:thesis:review')")
    public R<Void> deleteFile(@PathVariable Long fileId) {
        docFileService.deleteFile(fileId);
        return R.ok();
    }
}
