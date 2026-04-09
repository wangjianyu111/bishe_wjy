package com.gdplatform.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocFileService {
    Long uploadFile(MultipartFile file, String bizType, Long bizId, Long selectionId);
}
