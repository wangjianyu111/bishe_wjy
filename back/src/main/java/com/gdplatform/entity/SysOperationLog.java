package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 操作类型：LOGIN / QUERY / CREATE / UPDATE / DELETE / EXPORT / APPROVE / OTHER */
    private String operationType;

    /** 操作名称，如"登录系统"、"新增用户"、"审批通过" */
    private String operationName;

    /** 请求方法，如 GET/POST/PUT/DELETE */
    private String requestMethod;

    /** 请求路径 */
    private String requestUrl;

    /** 请求参数（JSON 字符串） */
    private String requestParams;

    /** 响应结果（JSON 字符串，仅记录成功结果摘要） */
    private String responseResult;

    /** 操作人用户ID */
    private Long userId;

    /** 操作人用户名 */
    private String userName;

    /** 操作人用户类型：1-学生 2-教师 3-管理员 */
    private Integer userType;

    /** 操作人所属学校 */
    private String campusName;

    /** IP 地址 */
    private String ipAddress;

    /** IP 归属地 */
    private String ipLocation;

    /** 操作系统 */
    private String os;

    /** 浏览器 */
    private String browser;

    /** 执行时长（毫秒） */
    private Long duration;

    /** 业务模块 */
    private String module;

    /** 操作状态：SUCCESS / FAIL */
    private String status;

    /** 错误信息 */
    private String errorMsg;

    private LocalDateTime operateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
