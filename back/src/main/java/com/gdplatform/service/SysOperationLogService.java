package com.gdplatform.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.OperationLogReq;
import com.gdplatform.dto.OperationLogResp;
import com.gdplatform.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperationLogService {

    Page<OperationLogResp> pageForAdmin(OperationLogReq req);

    void saveLog(SysOperationLog log);

    void deleteBatch(Long[] ids);
}
