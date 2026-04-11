package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.dto.OperationLogReq;
import com.gdplatform.dto.OperationLogResp;
import com.gdplatform.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    Page<OperationLogResp> selectPageForAdmin(Page<OperationLogResp> page, @Param("req") OperationLogReq req);
}
