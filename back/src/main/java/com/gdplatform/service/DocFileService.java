package com.gdplatform.service;

import com.gdplatform.entity.DocFile;
import org.springframework.web.multipart.MultipartFile;

public interface DocFileService {
    Long uploadFile(MultipartFile file, String bizType, Long bizId, Long selectionId);
    void deleteFile(Long fileId);
    DocFile getFileInfo(Long fileId);
}
