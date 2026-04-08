package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.TemplatePageReq;
import com.gdplatform.dto.TemplateResp;
import com.gdplatform.entity.TemplateFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateFileMapper extends BaseMapper<TemplateFile> {

    void insertTemplate(@Param("template") TemplateFile template);

    void updateTemplate(@Param("template") TemplateFile template);

    List<TemplateResp> selectTemplatePage(@Param("req") TemplatePageReq req,
                                          @Param("offset") long offset,
                                          @Param("limit") long limit);

    long countTemplatePage(@Param("req") TemplatePageReq req);

    TemplateResp selectRespById(@Param("templateId") Long templateId);
}
