package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.OperationLogReq;
import com.gdplatform.dto.OperationLogResp;
import com.gdplatform.entity.SysOperationLog;
import com.gdplatform.mapper.SysOperationLogMapper;
import com.gdplatform.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private final SysOperationLogMapper logMapper;

    @Override
    public Page<OperationLogResp> pageForAdmin(OperationLogReq req) {
        Page<OperationLogResp> page = new Page<>(req.getCurrent(), req.getSize());
        Page<OperationLogResp> result = logMapper.selectPageForAdmin(page, req);
        return result;
    }

    @Override
    public void saveLog(SysOperationLog log) {
        logMapper.insert(log);
    }

    @Override
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            logMapper.deleteById(id);
        }
    }
}
