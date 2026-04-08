package com.gdplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.TemplateAddReq;
import com.gdplatform.dto.TemplatePageReq;
import com.gdplatform.dto.TemplateResp;
import com.gdplatform.dto.TemplateUpdateReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TemplateFileService {

    TemplateResp uploadTemplate(MultipartFile file, TemplateAddReq req);

    void updateTemplate(TemplateUpdateReq req);

    void deleteTemplate(Long templateId);

    Page<TemplateResp> pageTemplate(TemplatePageReq req, long current, long size);

    TemplateResp getDetail(Long templateId);

    void downloadTemplate(Long templateId, HttpServletResponse response) throws IOException;

    void downloadStudentTemplate(Long templateId, String campusName, HttpServletResponse response) throws IOException;
}
