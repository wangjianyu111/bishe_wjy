package com.gdplatform.service.impl;

import com.gdplatform.common.BizException;
import com.gdplatform.entity.DocFile;
import com.gdplatform.mapper.DocFileMapper;
import com.gdplatform.service.DocFileService;
import com.gdplatform.util.SecurityUtils;
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
public class DocFileServiceImpl implements DocFileService {

    private final DocFileMapper docFileMapper;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "csv","pdf", "zip", "rar", "7z", "tar", "gz"
    );

    @Value("${gd.upload.path:./uploads}")
    private String uploadPath;

    private File getUploadDir() {
        Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
        File dir = base.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFile(MultipartFile file, String bizType, Long bizId, Long selectionId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();
        String ext = getFileExtension(originalName).toLowerCase();

        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BizException("不支持的文件格式，仅支持：doc/docx/xls/xlsx/pdf/zip/rar/7z/tar/gz");
        }

        long maxSize = 50 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            throw new BizException("文件大小不能超过 50MB");
        }

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dir = getUploadDir();
        File dateDir = new File(dir, datePath);
        if (!dateDir.exists()) {
            dateDir.mkdirs();
        }
        Path targetPath = dateDir.toPath().resolve(storedName);
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BizException("文件保存失败：" + e.getMessage());
        }

        com.gdplatform.entity.SysUser user = SecurityUtils.currentUser();

        DocFile docFile = new DocFile();
        docFile.setOriginalName(originalName);
        docFile.setStoredName(storedName);
        docFile.setFilePath(targetPath.toString());
        docFile.setFileSize(file.getSize());
        docFile.setMimeType(file.getContentType());
        docFile.setUploaderId(user.getUserId());
        docFile.setBizType(bizType);
        docFile.setBizId(bizId);
        docFile.setSelectionId(selectionId);
        docFile.setVersionNo(1);
        docFile.setCreateTime(LocalDateTime.now());

        docFileMapper.insert(docFile);
        return docFile.getFileId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        DocFile file = docFileMapper.selectByFileId(fileId);
        if (file == null) {
            throw new BizException("文件不存在");
        }
        Path path = Paths.get(file.getFilePath());
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                log.warn("物理文件删除失败: {}", file.getFilePath());
            }
        }
        docFileMapper.deleteById(fileId);
    }

    @Override
    public DocFile getFileInfo(Long fileId) {
        return docFileMapper.selectByFileId(fileId);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
